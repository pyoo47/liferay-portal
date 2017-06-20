/* Register class
 * This is the class which will hold tax data and perform calculations.
 * It will also be the class responsible for printing out the receipt.
 */
package challenge.coding.liferay;
import java.math.BigDecimal;
import java.util.Arrays;
import java.math.RoundingMode;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CashRegister{

  public static final BigDecimal BASICSALESTAX = new BigDecimal("0.10");
  public static final BigDecimal IMPORTSALESTAX = new BigDecimal("0.05");
  public static final BigDecimal ROUNDINGVALUE = new BigDecimal("0.05");

  public static BigDecimal calculateItemTax(CartItem inItem){
    BigDecimal itemTax = new BigDecimal("0.00");
    if(inItem.isTaxable()){
      itemTax = itemTax.add(calculateTax(inItem.getPreTaxPrice(), BASICSALESTAX));
    }

    if(inItem.isImported()){
      itemTax = itemTax.add(calculateTax(inItem.getPreTaxPrice(), IMPORTSALESTAX));
    }

    return itemTax;
  }

  public static BigDecimal roundTax(BigDecimal tax){
    BigDecimal moduloResult = tax.remainder(ROUNDINGVALUE);
    if(moduloResult.compareTo(BigDecimal.ZERO) != 0){
      tax = tax.add(ROUNDINGVALUE.subtract(moduloResult));
    }
    return tax;
  }

  public static BigDecimal scaleTax(BigDecimal tax){
    return tax.setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal calculateTax(BigDecimal principal, BigDecimal rate){
    BigDecimal total = principal.multiply(rate);
    total = scaleTax(total);
    total = roundTax(total);
    return total;
  }

  public static void printReceipt(Cart currentCart){
    BigDecimal itemTotal;
    for(CartItem inCart: currentCart.getContents()){
      itemTotal = new BigDecimal(inCart.getQuantity()).multiply(inCart.getPostTaxPrice()).setScale(2, RoundingMode.HALF_UP);
      String receiptLine = Integer.toString(inCart.getQuantity()) + " " + inCart.getName() + ": " + itemTotal.toString();
      System.out.println(receiptLine);
    }
    System.out.println("Sales Taxes: " + currentCart.getTotalTax().toString());
    System.out.println("Total: " + currentCart.getTotal().toString());
  }

  public static BigDecimal calculateCartTax(Cart customer){
    BigDecimal cartTax = new BigDecimal("0.00");
    BigDecimal itemTax;
    for(CartItem product: customer.contents){
      itemTax = calculateItemTax(product);
      cartTax = cartTax.add(itemTax);
      product.setPostTaxPrice(itemTax);
    }
    return cartTax;
  }

  public static void main(String[] args){
    Cart userCart = new Cart();
    //userCart.addToCart("1 book at 12.49");
    //userCart.addToCart("1 music CD at 14.99");
    //userCart.addToCart("1 chocolate bar at 0.85");

    //userCart.addToCart("1 imported box of chocolates at 10.00");
    //userCart.addToCart("1 imported bottle of perfume at 47.50");

    // userCart.addToCart("1 imported bottle of perfume at 27.99");
    // userCart.addToCart("1 bottle of perfume at 18.99");
    // userCart.addToCart("1 packet of headache pills at 9.75");
    // userCart.addToCart("1 imported box of chocolates at 11.25");
    Console console = null;
    String inputString = "";
    boolean interactiveMethod = false;
    boolean fileMethod = false;
    String fileName = "";

    // Differentiate between interactive mode and file mode
    // Priority goes to -interactive, if both are present

    for(String arg: args){
      if(arg.equals("-interactive")){
        interactiveMethod = true;
      }
      if(arg.equals("-file")){
        if( args.length > (Arrays.asList(args).indexOf("-file") + 1)){
          fileMethod = true;
          fileName = args[(Arrays.asList(args).indexOf("-file") + 1)];
        }
        else{
          System.out.println("Missing Filename Argument!");
          System.exit(1);
        }
      }
    }

    if(interactiveMethod){
      try{
        console = System.console();
        System.out.println("Interactive Mode: enter 'Done' to finish the cart");
        while(!(inputString.equals("Done") || inputString.equals("done"))){
          if(console != null){
            inputString = console.readLine("Please enter item: ");
            if(!(inputString.equals("Done") || inputString.equals("done"))){
              userCart.addToCart(inputString);
              System.out.println("Added!");
            } else {
              System.out.println("Thank you!");
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      userCart.setTotalTax(calculateCartTax(userCart));

      userCart.calculateTotal();
      printReceipt(userCart);
    }
    else if(fileMethod){
      try{
        Files.lines(Paths.get(fileName)).forEach((line) -> userCart.addToCart(line));
        userCart.setTotalTax(calculateCartTax(userCart));
        userCart.calculateTotal();
        printReceipt(userCart);
      } catch (IOException e){
        e.printStackTrace();
      }
    }
    else {
      System.out.println("Usage: Specify either interactive mode '-interactive' or file mode '-file'\n" +
                         "File mode requires a filename as an argument\nInteractive Mode takes precedence");
    }

  }

}
