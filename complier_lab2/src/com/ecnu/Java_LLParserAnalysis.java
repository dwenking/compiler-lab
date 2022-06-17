package com.ecnu;
import java.util.*;
import java.util.stream.Collectors;

public class Java_LLParserAnalysis
{
    private static List<String> prog = new ArrayList<>();

    /**
     *  this method is to read the standard input
     */
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String line = sc.nextLine();
            if ("".equals(line) || line.length() == 0) {
                continue;
            }
            prog.add(line.trim());
        }
    }

    /**
     *  you should add some code in this method to achieve this lab
     */
    private static void analysis()
    {
        read_prog();
        ParsingTable parsingTable = new ParsingTable();
        parsingTable.analysis(prog);
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }
}