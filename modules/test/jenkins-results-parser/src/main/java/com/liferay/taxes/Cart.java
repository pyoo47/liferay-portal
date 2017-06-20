/* Cart class
 * This is the class which will hold all necessary information for printing,
 * as well as the formatting.
 */
package challenge.coding.liferay;

import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.RoundingMode;


public class Cart{

  ArrayList<CartItem> contents;
  BigDecimal total;
  BigDecimal totalTax;

  public Cart(){
    contents = new ArrayList<CartItem>();
    total = new BigDecimal("0.00");
    totalTax = new BigDecimal("0.00");
  }

  public BigDecimal getTotal(){
    return total;
  }

  public ArrayList<CartItem> getContents(){
    return contents;
  }

  public BigDecimal getTotalTax(){
    return totalTax;
  }

  public void setTotalTax(BigDecimal taxTotal){
    totalTax = taxTotal;
  }

  public void calculateTotal(){
     BigDecimal cumulative = new BigDecimal("0.00");
     BigDecimal totalForItem;
    for(CartItem item: contents){
      totalForItem = new BigDecimal(item.getQuantity());
      totalForItem = totalForItem.multiply(item.getPostTaxPrice());
      totalForItem = totalForItem.setScale(2, RoundingMode.HALF_UP);
      cumulative = cumulative.add(totalForItem);
    }
    total = cumulative;
  }

  public void addToCart(CartItem newItem){
    contents.add(newItem);
  }

  public void removeFromCart(CartItem itemToRemove){
    contents.remove(itemToRemove);
  }

  public void addToCart(String entry){
    String[] brokenDownEntry = entry.split(" ");
    boolean isImported = false;
    String nameString = "";

// Get Quantity
    int quantity = Integer.parseInt(brokenDownEntry[0]);

// Get Name
    for(int i = 1; i < Arrays.asList(brokenDownEntry).indexOf("at"); i++){
      nameString = nameString + brokenDownEntry[i] + " ";
    }

    if(nameString != ""){
      nameString = nameString.substring(0, nameString.length() - 1);
    }

// Get price
    String price = brokenDownEntry[brokenDownEntry.length - 1];

    CartItem product = new CartItem(quantity, nameString, price);

    addToCart(product);
  }
}
