package Assembler;

import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.io.*;

/**
 * Created by luisf on 25/03/2018.
 */

/**
 * This is the class that handles all the necessary logic to translate assembler code to machine language.
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
    // Hash table to save the memory Addresses
    private HashMap<String, Short> symbolTable;
    // counter to keep position in ram
    private short posInTable;

    /**
     * Constructor fot the assembler class
     */
    public Assemble(){
        lines = new ArrayList<String>();
        cRules1 = new HashMap<String, String>();
        cRulesM = new HashMap<String, String>();
        jumpRules = new HashMap<String, String>();
        destRules = new HashMap<String, String>();
        assemblerCode = new ArrayList<String>();
        binaryCode = new ArrayList<String>();
        symbolTable = new HashMap<String, Short>();
        initializeValues();
        posInTable = 16;
        readRules();
    }

    /**
     * This method Initializes the tranlation table with common addresses between all assembler code
     */
    private void initializeValues(){
        symbolTable.put("SP", (short)0);
        symbolTable.put("LCL", (short)1);
        symbolTable.put("ARG", (short)2);
        symbolTable.put("THIS", (short)3);
        symbolTable.put("THAT", (short)4);
        for(int i = 0; i < 16; i++){
            symbolTable.put("R"+Integer.toString(i), (short)i);
        }
        symbolTable.put("SCREEN", (short)16384);
        symbolTable.put("KBD", (short)24576);
    }

    /**
     * This method reads the files that contain the rules for c type instructions
     */
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

    /**
     * This methods reads an assembler file and adds the lines to a list, ignoring the comments.
     * @param path path of the file that contains the assembler code
     * @return
     */
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
                        getComments[0] = StringUtils.trimWhitespace(getComments[0]);
                        assemblerCode.add(getComments[0]);
                    } else {
                        // no comments, just clean and add the instruction
                        line = line.replace(" ", "");
                        line = StringUtils.trimWhitespace(line);
                        assemblerCode.add(line);
                    }
                }
            }
            return true;
        }catch (FileNotFoundException e){
            return false;
        }catch(IOException e){
            return false;
        }

    }


    /**
     * This public method calls the necessary method to translate a file.
     * @param path path of the assembler code file
     */
    public void translate(String path){
        readFile(path);
        firstSwipe();
        secondSwipe();

    }

    /**
     * This method performs the first swipe on the data, adds tags to the symbolTable
     */
    private void firstSwipe(){
        // traverse the assembler code searching for tags, as they should be stored first in the translate table
        for(short i = 0; i < assemblerCode.size(); i++){
            if(assemblerCode.get(i).startsWith("(")){
                String tag = StringUtils.trimWhitespace(assemblerCode.get(i).replace("(", "").replace(")", ""));
                symbolTable.put(tag, (short)(i+1));
            }
        }
    }

    /**
     * This method performs the second swipe on the file, which should save and translate all variables of a Instructions
     */
    private void secondSwipe(){
        // traverse the assembler code searching for a type instructions
        for(int i = 0; i < assemblerCode.size(); i++){
            if(assemblerCode.get(i).startsWith("@")){
                // Check if the value is a variable or a tag
                String value = StringUtils.trimWhitespace(assemblerCode.get(i).replace("@", ""));
                // if the value exists in Symbol table, it is a tag, otherwise, a variable.
                if(symbolTable.containsKey(value)){
                    // Get the value of the tag and replace it in the assembler code.
                    assemblerCode.set(i, "@" + symbolTable.get(value).toString());
                }else {
                    // the value is a variable, add it to symbol table and update it in code
                    symbolTable.put(value, posInTable);
                    assemblerCode.set(i, "@" + Integer.toString(posInTable));
                    posInTable++;
                }
            }
        }
    }
}
