package maas;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import maas.tutorials.Book;

public class Start {
	private static final String sconstseller = "maas.tutorials.BookSellerAgent";
	private static final String sconstbuyer = "maas.tutorials.BookBuyerAgent";
	private static final String [] sBooktitle = new String[]{"The-Lord-of-the-rings", "The-Lord-of-the-rings-2", "The-Lord-of-the-rings-3", "Harry-Potter-1"};
	private static final int iNumberofBookSellerAgents = 4;
	private static final int iNumberofBookBuyerAgents = 30;
	private static final int iNumberofTitle = 4;
	private static Random rand = new Random();
	private static final Book[][] sbooks= new Book[][]{{new Book(sBooktitle[0], -1, rand.nextInt(10) + 1), new Book(sBooktitle[1], 3, rand.nextInt(10) + 1), new Book(sBooktitle[2], 10, rand.nextInt(10) + 1), new Book(sBooktitle[3], -1, rand.nextInt(10) + 1)},
			{new Book(sBooktitle[0], 3, rand.nextInt(10) + 1), new Book(sBooktitle[1], 3, rand.nextInt(10) + 1), new Book(sBooktitle[2], 3, rand.nextInt(10) + 1), new Book(sBooktitle[3], -1, rand.nextInt(10) + 1)},
			{new Book(sBooktitle[0], 3, rand.nextInt(10) + 1), new Book(sBooktitle[1], 3, rand.nextInt(10) + 1), new Book(sBooktitle[2], 3, rand.nextInt(10) + 1), new Book(sBooktitle[3], -1, rand.nextInt(10) + 1)},
			{new Book(sBooktitle[0], 3, rand.nextInt(10) + 1), new Book(sBooktitle[1], 3, rand.nextInt(10) + 1), new Book(sBooktitle[2], 3, rand.nextInt(10) + 1), new Book(sBooktitle[3], -1, rand.nextInt(10) + 1)}};
    public static void main(String[] args) {
    	List<String> agents = new Vector<>();
//		String [][] sbooks= new String [][]{{new Book()}};
//    	agents.add("testerbuyer:"+ sconstbuyer +"(The-Lord-of-the-rings)");
//    	agents.add("testerseller:"+sconstseller+"(The-Lord-of-the-rings, The-Lord-of-the-rings2)");
    	StringBuffer sbSeller = new StringBuffer();
		String sAgent = null;
    	// building inputstring

		for(int i = 0; i < iNumberofBookBuyerAgents; i++){
			agents.add("testerbuyer"+i+":"+ sconstbuyer +"("+sBooktitle[rand.nextInt(4)]+")");
		}
		for(int i = 0; i < iNumberofBookSellerAgents; i++){
			sbSeller.append("selleragent" + i + ":" + sconstseller + "(");
			for(int j = 0; j < iNumberofTitle; j++){
				sbSeller.append(sbooks[i][j].getsBookTitle() + " " + sbooks[i][j].getiQuantity() + " " + sbooks[i][j].getiPrice() + ",");
//				Book b = sbooks[i][j];
//				sbSeller.append(b.getsBookTitle())
			}
			sbSeller.append(")");
			sAgent = sbSeller.toString().replaceAll(",\\)", "\\)");
			agents.add(sAgent);
			sbSeller = new StringBuffer();
			sAgent = new String();
			// add Agent to vector
		}
    	List<String> cmd = new Vector<>();
    	cmd.add("-agents");
    	StringBuilder sb = new StringBuilder();
    	for (String a : agents) {
    		sb.append(a);
    		sb.append(";");
    	}
    	cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
}
