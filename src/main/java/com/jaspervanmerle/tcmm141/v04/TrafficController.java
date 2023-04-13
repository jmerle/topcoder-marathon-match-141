package com.jaspervanmerle.tcmm141.v04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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
    boolean isHorizontalOptimal;

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

        double probability = (double) spawnOptionsUsed / (double) spawnOptions;
        if (probability > 0.3) {
            probability = 0.3;
        }

        return probability;
    }
}

enum State {
    TURNING_HORIZONTAL,
    CALIBRATING_HORIZONTAL,
    TURNING_VERTICAL,
    CALIBRATING_VERTICAL,
    TURNING_OPTIMAL
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

    State state = State.TURNING_HORIZONTAL;
    int calibrationCounter = 0;
    boolean foundOptimalRoads = false;

    public Set<Cell> getLightsToSwitch() {
        if (state == State.TURNING_HORIZONTAL || state == State.TURNING_VERTICAL) {
            boolean done = true;

            for (Cell light : lights) {
                if (light.isHorizontal != (state == State.TURNING_HORIZONTAL)) {
                    done = false;
                    break;
                }
            }

            if (done) {
                state = state == State.TURNING_HORIZONTAL ? State.CALIBRATING_HORIZONTAL : State.CALIBRATING_VERTICAL;
                calibrationCounter = 0;
            }
        }

        if (state == State.CALIBRATING_HORIZONTAL || state == State.CALIBRATING_VERTICAL) {
            calibrationCounter++;
            if (calibrationCounter == 100) {
                state = state == State.CALIBRATING_HORIZONTAL ? State.TURNING_VERTICAL : State.TURNING_OPTIMAL;
            }
        }

        Set<Cell> lightsToSwitch = new HashSet<>();

        switch (state) {
            case TURNING_HORIZONTAL:
            case TURNING_VERTICAL:
                for (Cell light : lights) {
                    if (light.isHorizontal != (state == State.TURNING_HORIZONTAL)) {
                        lightsToSwitch.add(light);
                    }
                }
                break;
            case CALIBRATING_HORIZONTAL:
            case CALIBRATING_VERTICAL:
                for (Road road : roads) {
                    if (road.isHorizontal != (state == State.CALIBRATING_HORIZONTAL)) {
                        continue;
                    }

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
                break;
            case TURNING_OPTIMAL:
                if (!foundOptimalRoads) {
                    long start = System.nanoTime();

                    Set<Road> bestRoads = new HashSet<>();
                    double bestProbability = -1e9;

                    while (System.nanoTime() - start < 5e9) {
                        Set<Road> currentRoads = new HashSet<>();
                        double currentProbability = 0.0;

                        Collections.shuffle(roads);

                        for (Road road : roads) {
                            if (road.getSpawnProbability() == 0) {
                                continue;
                            }

                            boolean isUsable = true;
                            for (Cell cell : road.cells) {
                                if (!cell.hasLight) {
                                    continue;
                                }

                                Road intersectingRoad = road.isHorizontal ? cell.verticalRoad : cell.horizontalRoad;
                                if (currentRoads.contains(intersectingRoad)) {
                                    isUsable = false;
                                    break;
                                }
                            }

                            if (!isUsable) {
                                continue;
                            }

                            currentRoads.add(road);
                            currentProbability += road.getSpawnProbability();
                        }

                        if (currentProbability > bestProbability) {
                            bestRoads = currentRoads;
                            bestProbability = currentProbability;
                        }
                    }

                    for (Road road : bestRoads) {
                        for (Cell cell : road.cells) {
                            cell.isHorizontalOptimal = road.isHorizontal;
                        }
                    }

                    System.err.println("Probabilities:");
                    for (Road road : roads) {
                        Cell from = road.cells.get(0);
                        Cell to = road.cells.get(road.cells.size() - 1);
                        System.err.println("(" + from.x + ", " + from.y + ") to (" + to.x + ", " + to.y + "): " + road.getSpawnProbability());
                    }

                    System.err.println("Optimal roads:");
                    for (Road road : bestRoads) {
                        Cell from = road.cells.get(0);
                        Cell to = road.cells.get(road.cells.size() - 1);
                        System.err.println("(" + from.x + ", " + from.y + ") to (" + to.x + ", " + to.y + "): " + road.getSpawnProbability());
                    }

                    System.err.println("Optimal probability: " + bestProbability);

                    foundOptimalRoads = true;
                }

                for (Cell light : lights) {
                    if (light.isHorizontal != light.isHorizontalOptimal) {
                        lightsToSwitch.add(light);
                    }
                }
                break;
        }

        if (lightsToSwitch.isEmpty()) {
            return lightsToSwitch;
        }

        for (Road road : roads) {
            boolean carSeen = false;

            for (Cell cell : road.cells) {
                if (!cell.hasLight) {
                    continue;
                }

                if (cell.isHorizontal != road.isHorizontal) {
                    continue;
                }

                if (foundOptimalRoads && cell.isHorizontalOptimal != road.isHorizontal) {
                    continue;
                }

                carSeen = carSeen || (cell.car != null && cell.car.road == road);
                if (carSeen) {
                    lightsToSwitch.remove(cell);
                }
            }
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
