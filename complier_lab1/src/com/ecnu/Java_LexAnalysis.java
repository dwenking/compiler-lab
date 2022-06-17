package com.ecnu;
import java.util.*;


public class Java_LexAnalysis
{
    private static StringBuffer prog = new StringBuffer();
    private static MyScanner myScanner;

    /**
     *  this method is to read the standard input
     */
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String next = sc.nextLine();
            prog.append(next + '\n');
        }
    }

    /**
     *  you should add some code in this method to achieve this lab
     */
    private static void analysis()
    {
        read_prog();
        myScanner = new MyScanner(prog);
        myScanner.analysis();
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }
}

