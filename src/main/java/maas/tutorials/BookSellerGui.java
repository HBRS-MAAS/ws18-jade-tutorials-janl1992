package maas.tutorials;

public interface BookSellerGui {
    void setAgent(BookSellerAgent a);
    void show();
    void hide();
    void notifyUser(String message);
    void dispose();
}