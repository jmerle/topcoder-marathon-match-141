package com.jaspervanmerle.tcmm141.v08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

class InputReader {
    private BufferedReader br;
    private StringTokenizer st;

    public InputReader(InputStream in) {
        br = new BufferedReader(new InputStreamReader(in));
    }

    public String next() {
        while (st == null || !st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return st.nextToken();
    }

    public int nextInt() {
        return Integer.parseInt(next());
    }

    public double nextDouble() {
        return Double.parseDouble(next());
    }
}

class Cell {
    int x;
    int y;

    boolean hasLight;
    boolean isHorizontal;

    Car car;
    Car previousCar;

    Road horizontalRoad;
    Road verticalRoad;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Car {
    int value;
    Cell cell;
    Road road;

    public Car(int value, Cell cell, Road road) {
        this.value = value;
        this.cell = cell;
        this.road = road;
    }
}

class Road {
    boolean isHorizontal;
    boolean isReverse;

    List<Cell> cells = new ArrayList<>();
    List<Car> cars = new ArrayList<>();

    int spawnOptions = 0;
    int spawnOptionsUsed = 0;

    public Road(boolean isHorizontal, boolean isReverse) {
        this.isHorizontal = isHorizontal;
        this.isReverse = isReverse;
    }

    public double getSpawnProbability() {
        if (spawnOptions == 0) {
            return 0;
        }

        return Math.min(0.3, (double) spawnOptionsUsed / (double) spawnOptions);
    }

    public Cell getPreviousCell(Cell cell) {
        int index = cells.indexOf(cell);
        return index > 0 ? cells.get(index - 1) : null;
    }

    public Cell getNextCell(Cell cell) {
        int index = cells.indexOf(cell);
        return index < cells.size() - 1 ? cells.get(index + 1) : null;
    }
}

public class TrafficController {
    int gridSize;
    int noRoads;
    double ambulanceProbability;
    int ambulanceValue;

    Cell[][] grid;

    List<Cell> lights = new ArrayList<>();
    List<Car> cars = new ArrayList<>();
    List<Road> roads = new ArrayList<>();

    int turn;
    int totalElapsedTime;

    public Set<Cell> getLightsToSwitch() {
        if (turn == 0) {
            Set<Cell> lightsToSwitch = new HashSet<>();

            for (Cell light : lights) {
                if (!light.isHorizontal) {
                    lightsToSwitch.add(light);
                }
            }

            return lightsToSwitch;
        }

        Set<Cell> blockedLights = new HashSet<>();
        for (Car car : cars) {
            if (!car.cell.hasLight) {
                continue;
            }

            Road road = car.road;
            for (int i = road.cells.indexOf(car.cell); i < road.cells.size(); i++) {
                Cell cell = road.cells.get(i);
                if (!cell.hasLight || cell.isHorizontal != road.isHorizontal) {
                    break;
                }

                blockedLights.add(cell);
            }
        }

        Set<Cell> lightsToSwitch = new HashSet<>();

        outer:
        for (Road road : roads) {
            Set<Cell> lightsToSwitchRoad = new HashSet<>();

            boolean carSeen = false;
            for (Cell cell : road.cells) {
                carSeen = carSeen || (cell.car != null && cell.car.road == road);

                if (!cell.hasLight || !carSeen || cell.isHorizontal == road.isHorizontal) {
                    continue;
                }

                if (blockedLights.contains(cell)) {
                    continue outer;
                }

                lightsToSwitchRoad.add(cell);
            }

            lightsToSwitch.addAll(lightsToSwitchRoad);
        }

        return lightsToSwitch;
    }

    public void run() {
        InputReader in = new InputReader(System.in);

        gridSize = in.nextInt();
        noRoads = in.nextInt();
        ambulanceProbability = in.nextDouble();
        ambulanceValue = in.nextInt();

        grid = new Cell[gridSize][gridSize];
        for (int y = 0; y < gridSize; y++) {
            Cell[] row = new Cell[gridSize];

            for (int x = 0; x < gridSize; x++) {
                row[x] = new Cell(x, y);
            }

            grid[y] = row;
        }

        char[][] initialGrid = new char[gridSize][gridSize];
        for (int y = 0; y < gridSize; y++) {
            char[] row = new char[gridSize];

            for (int x = 0; x < gridSize; x++) {
                row[x] = in.next().charAt(0);
                System.err.print(row[x]);

                grid[y][x].hasLight = row[x] == '-' || row[x] == '|';
                grid[y][x].isHorizontal = row[x] == '-';

                if (grid[y][x].hasLight) {
                    lights.add(grid[y][x]);
                }
            }

            System.err.println();
            initialGrid[y] = row;
        }

        for (int y = 0; y < gridSize; y++) {
            Road currentRoad = null;

            for (int x = 0; x < gridSize; x++) {
                char ch = initialGrid[y][x];

                if (ch == '>' || ch == '<' || (currentRoad != null && (ch == '-' || ch == '|'))) {
                    if (currentRoad == null || (currentRoad.isReverse && ch == '>') || (!currentRoad.isReverse && ch == '<')) {
                        currentRoad = new Road(true, ch == '<');
                        roads.add(currentRoad);
                    }

                    if (currentRoad.isReverse) {
                        currentRoad.cells.add(0, grid[y][x]);
                    } else {
                        currentRoad.cells.add(grid[y][x]);
                    }

                    grid[y][x].horizontalRoad = currentRoad;
                } else {
                    currentRoad = null;
                }
            }
        }

        for (int x = 0; x < gridSize; x++) {
            Road currentRoad = null;

            for (int y = 0; y < gridSize; y++) {
                char ch = initialGrid[y][x];

                if (ch == 'v' || ch == '^' || (currentRoad != null && (ch == '-' || ch == '|'))) {
                    if (currentRoad == null || (currentRoad.isReverse && ch == 'v') || (!currentRoad.isReverse && ch == '^')) {
                        currentRoad = new Road(false, ch == '^');
                        roads.add(currentRoad);
                    }

                    if (currentRoad.isReverse) {
                        currentRoad.cells.add(0, grid[y][x]);
                    } else {
                        currentRoad.cells.add(grid[y][x]);
                    }

                    grid[y][x].verticalRoad = currentRoad;
                } else {
                    currentRoad = null;
                }
            }
        }

        for (turn = 0; turn < 1000; turn++) {
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    grid[y][x].previousCar = grid[y][x].car;
                }
            }

            for (Car car : cars) {
                car.cell.car = null;
            }

            for (Road road : roads) {
                road.cars.clear();
            }

            cars.clear();

            int noCars = in.nextInt();
            for (int i = 0; i < noCars; i++) {
                int y = in.nextInt();
                int x = in.nextInt();
                int value = in.nextInt();
                char direction = in.next().charAt(0);

                Cell cell = grid[y][x];
                Road road = direction == '>' || direction == '<' ? cell.horizontalRoad : cell.verticalRoad;

                Car car = new Car(value, cell, road);

                cars.add(car);
                cell.car = car;
                road.cars.add(car);
            }

            totalElapsedTime = in.nextInt();

            for (Road road : roads) {
                boolean hadSpawnOption = false;
                if (road.cells.get(0).previousCar != null) {
                    for (Cell cell : road.cells) {
                        if (cell.previousCar == null) {
                            hadSpawnOption = cell.car != null && cell.car.road == road;
                            break;
                        }
                    }
                } else {
                    hadSpawnOption = true;
                }

                if (hadSpawnOption) {
                    road.spawnOptions++;
                    if (road.cells.get(0).car != null) {
                        road.spawnOptionsUsed++;
                    }
                }
            }

            Set<Cell> lightsToSwitch = getLightsToSwitch();

            System.out.println(lightsToSwitch.size());
            for (Cell light : lightsToSwitch) {
                System.out.println(light.y + " " + light.x);

                light.isHorizontal = !light.isHorizontal;
            }
        }
    }

    public static void main(String[] args) {
        new TrafficController().run();
    }
}
