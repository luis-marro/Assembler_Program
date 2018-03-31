package Assembler;

import java.util.*;
import java.io.*;

/**
 * Created by luisf on 25/03/2018.
 */
public class Assemble {
    private List<String> lines;
    private String[] translated;
    // Hash tables to store the rules
    private HashMap<String, String> cRules1;
    private HashMap<String, String> cRulesM;
    private HashMap<String, String> jumpRules;
    private HashMap<String, String> destRules;
    // list that will hold the code
    private List<String> assemblerCode;
    private List<String> binaryCode;


    public Assemble(){
        lines = new ArrayList<String>();
        cRules1 = new HashMap<String, String>();
        cRulesM = new HashMap<String, String>();
        jumpRules = new HashMap<String, String>();
        destRules = new HashMap<String, String>();
        assemblerCode = new ArrayList<String>();
        binaryCode = new ArrayList<String>();
        readRules();
    }

    private void readRules(){
        // first, read the c Instruction rules
        File actual = new File("src/C_Rules.txt");
        try{
            FileInputStream input = new FileInputStream(actual);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            // Read the cRules file
            while((line = reader.readLine()) != null){
                String[] definition = line.split("_");
                // first, add Crules 1
                cRules1.put(definition[0], definition[1]);
                // Check if the rule has M translation
                if(!definition[2].equals("n")){
                    cRulesM.put(definition[2], definition[1]);
                }
            }
            // now read the dest instructions
            actual = new File("src/dest_instr.txt");
            input = new FileInputStream(actual);
            reader = new BufferedReader(new InputStreamReader(input));
            // Now read that file
            while((line = reader.readLine()) != null){
                String[] splitted = line.split("\\|");
                // Add the rule
                destRules.put(splitted[0], splitted[1]);
            }
            // Finally for jump rules
            actual = new File("src/jump_instr.txt");
            input = new FileInputStream(actual);
            reader = new BufferedReader(new InputStreamReader(input));
            // Read the file
            while((line = reader.readLine()) != null){
                String[] splitted = line.split("\\|");
                // Add the rule
                jumpRules.put(splitted[0], splitted[1]);
            }
            System.out.print("all files read!");
        } catch (FileNotFoundException e){
            System.out.print("El archivo leido no existe");
        } catch (IOException e){
            System.out.print("Error Leyendo el archivo");
        }

    }

    private boolean readFile(String path){
        File toTranslate = new File(path);
        try {
            FileInputStream input = new FileInputStream(toTranslate);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            // read the file
            String line = null;
            while((line = reader.readLine()) != null){
                if(!line.isEmpty()) {
                    // Check if the line is a comment
                    if (line.startsWith("//"))
                        continue;
                    // the line isn't a comment, add it to the list
                    // first, check that the line doesn't contain commente, add only the instruction
                    if (line.contains("//")) {
                        // split the string with the comment separator
                        String[] getComments = line.split("//");
                        // the first position of the array should be the instruction
                        // it can contain blank spaces, delete them
                        getComments[0] = getComments[0].replace(" ", "");
                        getComments[0] = getComments[0].trim();
                        assemblerCode.add(getComments[0]);
                    } else {
                        // no comments, just clean and add the instruction
                        line = line.replace(" ", "");
                        line = line.trim();
                        assemblerCode.add(line);
                    }
                }
            }
            for(int i = 0; i < assemblerCode.size(); i++){
                System.out.println(assemblerCode.get(i));
            }
            return true;
        }catch (FileNotFoundException e){
            return false;
        }catch(IOException e){
            return false;
        }

    }

    public void translate(String path){
        readFile(path);


    }
}
