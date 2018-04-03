package Assembler;

import com.sun.deploy.util.StringUtils;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

/**
 * Created by luisf on 25/03/2018.
 */

/**
 * This is the class that handles all the necessary logic to translate assembler code to machine language.
 */
public class Assemble {
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
    private String finalPath;

    /**
     * Constructor fot the assembler class
     */
    public Assemble(){
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
     * Public get to display the translated binary code.
     * @return translated binary code.
     */
    public List<String> getBinaryCode(){
        return binaryCode;
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
        finalPath = path;
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
        finalTranslation();
        saveFile();
    }

    /**
     * This method performs the first swipe on the data, adds tags to the symbolTable
     */
    private void firstSwipe(){
        // traverse the assembler code searching for tags, as they should be stored first in the translate table
        short counter = 0;
        for(short i = 0; i < assemblerCode.size(); i++){
            if(assemblerCode.get(i).startsWith("(")){
                String tag = StringUtils.trimWhitespace(assemblerCode.get(i).replace("(", "").replace(")", ""));
                symbolTable.put(tag, counter);
                counter--;
            }
            counter++;
        }
    }

    /**
     * This method performs the second swipe on the file, which should save and translate all variables
     * in the code
     */
    private void secondSwipe(){
        // traverse the assembler code searching for a type instructions
        for(int i = 0; i < assemblerCode.size(); i++){
            if(assemblerCode.get(i).startsWith("@")){
                // Check if the value is a variable or a tag or a number
                String value = StringUtils.trimWhitespace(assemblerCode.get(i).replace("@", ""));
                // First, check if the value is a number
                if(org.apache.commons.lang3.StringUtils.isNumericSpace(value)){
                    // it is a number, if so, just skip the line
                    continue;
                }
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

    /**
     * This method handles the logic of converting the assembler code to binary
     */
    private void finalTranslation(){
        for(int i = 0; i < assemblerCode.size(); i++){
            // the line should not be a tag to be translated, check the existence of a parentheses.
            if(!assemblerCode.get(i).startsWith("(")){
                // the line should be translated
                // Check if it is a c type instruction or a type
                if(assemblerCode.get(i).startsWith("@")){
                    // a type instruction
                    // first, convert to binary
                    String binary = StringUtils.trimWhitespace(assemblerCode.get(i).replace("@", ""));
                    binary = Integer.toBinaryString(Integer.parseInt(binary));
                    // the number should always be 16 bits
                    if(binary.length() < 16){
                        for(int j = binary.length(); j < 16; j++)
                            binary = "0" + binary;
                    }
                    // finally, add the binary to the output
                    binaryCode.add(binary);
                }else{
                    // The instruction is a c type instruction
                    // begin building the string
                    StringBuilder builder = new StringBuilder("111");
                    // check the rule for the next 6 bits
                    if(assemblerCode.get(i).contains("M") && !assemblerCode.get(i).contains("J")){
                        // Contains M, a = 1, search the RulesM hashmap
                        String[] instruction = assemblerCode.get(i).split("=");
                        // special case where M is the destination and the operation
                        if(instruction[0].equals("M")){
                            if(!instruction[1].contains("M")) {
                                builder.append("0");
                                builder.append(cRules1.get(instruction[1]));
                                // add the extra bits
                                builder.append(destRules.get(instruction[0]));
                            }else{
                                builder.append("1");
                                builder.append(cRulesM.get(instruction[1]));
                                builder.append(destRules.get(instruction[0]));
                            }
                            builder.append("000");
                        }else if(instruction[0].equals("MD") || instruction[0].equals("AM") || instruction[0].equals("AMD")){
                            if(instruction[1].contains("D")){
                                builder.append("0");
                                builder.append(cRules1.get(instruction[1]));
                                builder.append(destRules.get(instruction[0]));
                                builder.append("000");
                            }else {
                                builder.append("1");
                                builder.append(cRulesM.get(instruction[1]));
                                builder.append(destRules.get(instruction[0]));
                                builder.append("000");
                            }
                        }
                        else {
                            builder.append("1");
                            builder.append(cRulesM.get(instruction[1]));
                            // add the destination bits
                            builder.append(destRules.get(instruction[0]));
                            builder.append("000");
                        }
                    }else{
                        builder.append("0");
                        // check if it a jump or an assignation
                        if(assemblerCode.get(i).contains(";")){
                            String[] instruction = assemblerCode.get(i).split(";");
                            builder.append(cRules1.get(instruction[0]));
                            builder.append("000");
                            builder.append(jumpRules.get(instruction[1]));
                        }else{
                            // the operation has a destination
                            String[] instruction = assemblerCode.get(i).split("=");
                            builder.append(cRules1.get(instruction[1]));
                            builder.append(destRules.get(instruction[0]));
                            builder.append("000");
                        }
                    }
                    binaryCode.add(builder.toString());
                }
            }
        }
    }

    /**
     * This method saves the output binary in a new file with extension .hack
     */
    private void saveFile(){
        finalPath = finalPath.replace(".asm", ".hack");
        Path toWrite = Paths.get(finalPath);
        if(new File(finalPath).isFile()){
            new File(finalPath).delete();
        }
        if(binaryCode.size() > 0) {
            try {
                Files.write(toWrite, binaryCode, Charset.forName("UTF-8"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


}
