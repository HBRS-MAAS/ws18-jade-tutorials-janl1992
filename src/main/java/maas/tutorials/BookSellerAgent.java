package maas.tutorials;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class BookSellerAgent extends Agent {
    private Hashtable catalogue;
    private BookSellerGui myGui;

    protected void setup() {
        catalogue = new Hashtable();
        myGui = new BookSellerGuiImpl();
        // ist das so richtig?
        myGui.setAgent(this);
//        myGui.show();
        Object[] oArguments = getArguments();
        String[] sSplit;
        // muss noch implementiert werden
//        System.out.println(oArguments[0].toString());
//        System.out.println(oArguments[1].toString());
        for (Object oArgument:
             oArguments) {
            String oArgument1 = (String) oArgument;
            sSplit = oArgument1.split(" ");
            Book b = new Book(sSplit[0], Integer.valueOf(sSplit[1]), Integer.valueOf(sSplit[2]));
            updateCatalogue(b);

        }
//        updateCatalogue((String) oArguments[0], 10);
//        updateCatalogue((String) oArguments[1], 10);
         addBehaviour(new OfferRequestServer());
//         addBehaviour(new PurchaseOrdersServer());
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("BookSellerAgent ready");
    }
    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
//        myGui.dispose();
//        System.out.println("Seller Agent" + getAID().getName() + "terminating");
    }
    public void updateCatalogue(final Book bK){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                catalogue.put(bK.getsBookTitle(), new Book(bK.getsBookTitle(), bK.getiQuantity(), bK.getiPrice()));
            }
        });
    }

    private class OfferRequestServer extends CyclicBehaviour {

        @Override
        public void action() {
//            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive();
            if(msg != null && (msg.getPerformative() == ACLMessage.CFP)){
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = null;
                if (catalogue.get(title) != null ) {
                    Book b = (Book)catalogue.get(title);
                    price = b.getiPrice();
                    if(price != null && b.getiQuantity() != 0) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(String.valueOf(price.intValue()));
                    } else {
                        refuseoffer(reply);
                    }
                }
                else {
                    refuseoffer(reply);
                }
//                if(MessageTemplate.MatchPerformative(msg.getPerformative()))

                myAgent.send(reply);
            }
            else {
                block();
            }
            if(msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                String sTitleBuy = msg.getContent();
                Book bTarget = (Book) catalogue.get(sTitleBuy);
                if(bTarget.getiQuantity() != (-1) && bTarget.getiQuantity() != 0){
                    int iQuantity = bTarget.getiQuantity();
                    bTarget.setiQuantity(iQuantity - 1);
                }
                updateCatalogue(bTarget);
                ACLMessage aclTargetReply = msg.createReply();
                aclTargetReply.setPerformative(ACLMessage.INFORM);
                aclTargetReply.setContent(String.valueOf(bTarget.getiPrice()));
                myAgent.send(aclTargetReply);
                for (Object oBook:
                     catalogue.values()) {
                    Book b = (Book) oBook;
                    if(b.getiQuantity() == 0){
                        System.out.println(getAID().getName() + " is terminated as quantity of " + b.getsBookTitle() + " is 0");
                        doDelete();
                        break;
                    }

                }
            }
        }
    }

    private void refuseoffer(ACLMessage reply) {
        reply.setPerformative(ACLMessage.REFUSE);
        reply.setContent("not-available");
    }
}

