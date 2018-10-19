package maas.tutorials;

public class Book {
    public void setiQuantity(int iQuantity) {
        this.iQuantity = iQuantity;
    }

    private String sBookTitle;
    private int iQuantity;
    private int iPrice;

    public String getsBookTitle() {
        return sBookTitle;
    }

    public int getiQuantity() {
        return iQuantity;
    }


    public int getiPrice() {
        return iPrice;
    }

    public Book(String sBookTitle, int iType, int iPrice) {
        this.sBookTitle = sBookTitle;
        this.iQuantity = iType;
        this.iPrice = iPrice;
    }
}
