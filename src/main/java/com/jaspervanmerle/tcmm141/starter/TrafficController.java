package com.jaspervanmerle.tcmm141.starter;

import java.io.*;
import java.util.*;

public class TrafficController
{
    public static void main(String[] args) throws Exception
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        int N=Integer.parseInt(in.readLine());
        int R=Integer.parseInt(in.readLine());
        double AP=Double.parseDouble(in.readLine());
        int AV=Integer.parseInt(in.readLine());

        //read grid
        List<Integer> lights=new ArrayList<Integer>();
        char[][] grid=new char[N][N];
        for (int r=0; r<N; r++)
            for (int c=0; c<N; c++)
            {
                grid[r][c]=in.readLine().charAt(0);
                if (grid[r][c]=='|' || grid[r][c]=='-') lights.add(r*N+c);
            }

        for (int turn=1; turn<=1000; turn++)
        {
            //read car locations
            int cars=Integer.parseInt(in.readLine());
            for (int i=0; i<cars; i++)
            {
                String[] temp=in.readLine().split(" ");
                int carR=Integer.parseInt(temp[0]);
                int carC=Integer.parseInt(temp[1]);
                int carVal=Integer.parseInt(temp[2]);
                char carDir=temp[3].charAt(0);
            }
            int elapsedTime=Integer.parseInt(in.readLine());

            int pos=lights.get((turn-1)%lights.size());
            int r=pos/N;
            int c=pos%N;
            System.out.println("1");
            System.out.println(r+" "+c);
            System.out.flush();
        }
    }
}
