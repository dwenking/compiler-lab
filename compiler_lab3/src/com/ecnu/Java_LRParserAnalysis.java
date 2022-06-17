package com.ecnu;
import java.util.*;

public class Java_LRParserAnalysis
{
    private static List<String[]> exprs = new ArrayList<>();

    /**
     *  this method is to read the standard input
     */
    private static void read_prog()
    {
        int cnt = 0;
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String line = sc.nextLine();
            if (line.length() == 0 || "".equals(line)) {
                continue;
            }
            for (String str : line.split("\\s+")) {
                exprs.add(new String[]{str, String.valueOf(cnt)});
            }
            cnt++;
        }
    }

    /**
     *  you should add some code in this method to achieve this lab
     */
    private static void analysis() throws Exception {
        read_prog();
        ParsingTable table = new ParsingTable();
        table.analysis(exprs);
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) throws Exception {
        analysis();
    }
}
