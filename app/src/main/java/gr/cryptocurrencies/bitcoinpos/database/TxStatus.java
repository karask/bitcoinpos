package gr.cryptocurrencies.bitcoinpos.database;



public interface TxStatus {
    public final int CONFIRMED = 1; // previously set to 1 when true if tx was confirmed
    public final int PENDING = 2;   // NOT EXISTING STATE previously
    public final int ONGOING = 0;   // previously set to 0 when false if tx was unconfirmed/ongoing
    public final int CANCELLED = 3; // NOT EXISTING STATE previously

//TODO SET VALUES TO:
//    public final int PENDING = 0;
//    public final int ONGOING = 1;
//    public final int CONFIRMED = 2;
//    public final int CANCELLED = 3;
}
