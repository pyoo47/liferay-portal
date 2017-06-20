/* Item class
 * This is the class of which every item in the basket will be an instance.
 */
package challenge.coding.liferay;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.StandardOpenOption;

 public class CartItem {

    static String FOODFILE = "food_file.txt";
    static String MEDICINEFILE = "medicine_file.txt";
    static String SEENFILE = "all_seen_products.txt";
    StandardOpenOption[] options = new StandardOpenOption[] { APPEND, CREATE};
    boolean basicSalesTaxApplicable;
    boolean importSalesTaxApplicable;
    String name;
    BigDecimal preTaxPrice;
    BigDecimal postTaxPrice;
    int quantity;

    public CartItem(int amount, String productName, String extPrice){
      quantity = amount;
      name = productName;
      basicSalesTaxApplicable = checkBasicTaxability(productName);
      importSalesTaxApplicable = checkImportTaxability(productName);
      preTaxPrice = new BigDecimal(extPrice);
    }

    public BigDecimal getPreTaxPrice(){
      return preTaxPrice;
    }

    public void setPostTaxPrice(BigDecimal taxTotal){
      postTaxPrice = preTaxPrice.add(taxTotal);
    }

    public BigDecimal getPostTaxPrice(){
      return postTaxPrice;
    }

    public boolean isTaxable(){
      return basicSalesTaxApplicable;
    }

    public boolean isImported(){
      return importSalesTaxApplicable;
    }

    public String getName(){
      return name;
    }

    public int getQuantity(){
      return quantity;
    }

    private boolean checkImportTaxability(String productName){
      if(Arrays.asList(productName.split(" ")).contains("imported")){
        return true;
      }
      else {
        return false;
      }
    }

    public boolean checkBasicTaxability(String productName){
      if(checkIfSeen(productName)){
        return (!(checkIfFood(productName) || checkIfMedicine(productName) || checkIfBook(productName)));
      }
      else {
        addToSeen(productName);
        if(checkIfBook(productName)){
          return false;
        }
        else if(askIfFood(productName)){
          addToFood(productName);
          return false;
        }
        else if(askIfMedicine(productName)){
          addToMedicine(productName);
          return false;
        }
        else{
          return true;
        }
      }
    }

    private boolean checkIfBook(String productName){
      return (productName.equals("book") || productName.equals("books"));
    }

    private boolean checkIfSeen(String productName){
      // Consult SEEN file
      boolean existsInSeen = false;
      try{
        existsInSeen = Files.lines(Paths.get(SEENFILE)).anyMatch((item) -> item.equals(productName));
      } catch (IOException e){
        //e.printStackTrace();
        try{
          Files.createFile(Paths.get(SEENFILE));
          checkIfSeen(productName);
        } catch (IOException x){
          x.printStackTrace();
        }
      }
      return existsInSeen;
    }

    private boolean checkIfFood(String productName){
      // consult Food file
      boolean existsInFood = false;
      try{
        existsInFood = Files.lines(Paths.get(FOODFILE)).anyMatch((item) -> item.equals(productName));
      } catch (IOException e){
        //e.printStackTrace();
        try{
          Files.createFile(Paths.get(FOODFILE));
          checkIfFood(productName);
        } catch (IOException x){
          x.printStackTrace();
        }
      }
      return existsInFood;
    }

    private boolean checkIfMedicine(String productName){
      // Consult MEDICINE file
      boolean existsInMedicine = false;
      try{
        existsInMedicine = Files.lines(Paths.get(MEDICINEFILE)).anyMatch((item) -> item.equals(productName));
      } catch (IOException e){
        //e.printStackTrace();
        try{
          Files.createFile(Paths.get(MEDICINEFILE));
          checkIfMedicine(productName);
        } catch (IOException x){
          x.printStackTrace();
        }
      }
      return existsInMedicine;
    }

    private boolean askIfFood(String productName){
      //Ask via commandline if this product is Food
      System.out.println("This product has not been seen before. Please answer the following for tax purposes:");
      boolean answer = false;
      Console console = null;
      String response = null;
      try {
        console = System.console();
        if(console != null){
          response = console.readLine("Is " + productName + " food: ");
          if(response.equals("yes") || response.equals("Yes")){
            answer = true;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return answer;
    }

    private boolean askIfMedicine(String productName){
      // Ask via commandline if this product is Medicine
      boolean answer = false;
      Console console = null;
      String response = null;
      try {
        console = System.console();
        if(console != null){
          response = console.readLine("Is " + productName + " medicine: ");
          if(response.equals("yes") || response.equals("Yes")){
            answer = true;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return answer;
    }

    private void addToSeen(String productName){
      // Add to SEEN file
      ArrayList<String> iterableProduct = new ArrayList<String>();
      iterableProduct.add(productName);
      try{
        Files.write(Paths.get(SEENFILE), iterableProduct, options);
      } catch(Exception e){
        e.printStackTrace();
      }
    }

    private void addToFood(String productName){
      // Add to Food file
      ArrayList<String> iterableProduct = new ArrayList<String>();
      iterableProduct.add(productName);
      try{
        Files.write(Paths.get(FOODFILE), iterableProduct, options);
      } catch(Exception e){
        e.printStackTrace();
      }
    }

    private void addToMedicine(String productName){
      // Add to Medicine file
      ArrayList<String> iterableProduct = new ArrayList<String>();
      iterableProduct.add(productName);
      try{
        Files.write(Paths.get(MEDICINEFILE), iterableProduct, options);
      } catch(Exception e){
        e.printStackTrace();
      }
    }
 }
