package com.jaspervanmerle.tcmm141.v01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

    public long nextLong() {
        return Long.parseLong(next());
    }

    public float nextFloat() {
        return Float.parseFloat(next());
    }

    public double nextDouble() {
        return Double.parseDouble(next());
    }

    public String nextLine() {
        String line;

        try {
            line = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return line;
    }
}

class Cell {
    int x;
    int y;

    boolean hasLight;
    boolean isHorizontal;

    Car car;

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
    List<Cell> cells = new ArrayList<>();
    List<Car> cars = new ArrayList<>();
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

    public void performTurn() {
        List<Cell> lightsToSwitch = new ArrayList<>();

        for (Cell light : lights) {
            if (light.car == null) {
                lightsToSwitch.add(light);
            }
        }

        System.out.println(lightsToSwitch.size());
        for (Cell light : lightsToSwitch) {
            System.out.println(light.y + " " + light.x);
        }
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

                if (row[x] == '-') {
                    grid[y][x].hasLight = true;
                    grid[y][x].isHorizontal = true;
                } else if (row[x] == '|') {
                    grid[y][x].hasLight = true;
                    grid[y][x].isHorizontal = false;
                }
            }

            initialGrid[y] = row;
        }

        for (int y = 0; y < gridSize; y++) {
            Road currentRoad = null;

            for (int x = 0; x < gridSize; x++) {
                if (initialGrid[y][x] == '>' || (currentRoad != null && (initialGrid[y][x] == '-' || initialGrid[y][x] == '|'))) {
                    if (currentRoad == null) {
                        currentRoad = new Road();
                        roads.add(currentRoad);
                    }

                    currentRoad.cells.add(grid[y][x]);
                    grid[y][x].horizontalRoad = currentRoad;
                } else {
                    currentRoad = null;
                }
            }
        }

        for (int y = 0; y < gridSize; y++) {
            Road currentRoad = null;

            for (int x = gridSize - 1; x >= 0; x--) {
                if (initialGrid[y][x] == '<' || (currentRoad != null && (initialGrid[y][x] == '-' || initialGrid[y][x] == '|'))) {
                    if (currentRoad == null) {
                        currentRoad = new Road();
                        roads.add(currentRoad);
                    }

                    currentRoad.cells.add(grid[y][x]);
                    grid[y][x].horizontalRoad = currentRoad;
                } else {
                    currentRoad = null;
                }
            }
        }

        for (int x = 0; x < gridSize; x++) {
            Road currentRoad = null;

            for (int y = 0; y < gridSize; y++) {
                if (initialGrid[y][x] == 'v' || (currentRoad != null && (initialGrid[y][x] == '-' || initialGrid[y][x] == '|'))) {
                    if (currentRoad == null) {
                        currentRoad = new Road();
                        roads.add(currentRoad);
                    }

                    currentRoad.cells.add(grid[y][x]);
                    grid[y][x].verticalRoad = currentRoad;
                } else {
                    currentRoad = null;
                }
            }
        }

        for (int x = 0; x < gridSize; x++) {
            Road currentRoad = null;

            for (int y = gridSize - 1; y >= 0; y--) {
                if (initialGrid[y][x] == '^' || (currentRoad != null && (initialGrid[y][x] == '-' || initialGrid[y][x] == '|'))) {
                    if (currentRoad == null) {
                        currentRoad = new Road();
                        roads.add(currentRoad);
                    }

                    currentRoad.cells.add(grid[y][x]);
                    grid[y][x].verticalRoad = currentRoad;
                } else {
                    currentRoad = null;
                }
            }
        }

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                if (grid[y][x].hasLight) {
                    lights.add(grid[y][x]);
                }
            }
        }

        for (turn = 0; turn < 1000; turn++) {
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

            performTurn();
        }
    }

    public static void main(String[] args) {
        new TrafficController().run();
    }
}
