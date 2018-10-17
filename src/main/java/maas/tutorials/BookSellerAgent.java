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
        myGui.show();
        // muss noch implementiert werden
        updateCatalogue("The-Lord-of-the-rings", 10);
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
        myGui.dispose();
        System.out.println("Seller Agent" + getAID().getName() + "terminating");
    }
    public void updateCatalogue(final String title, final int price){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                catalogue.put(title, new Integer(price));
            }
        });
    }

    private class OfferRequestServer extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive();
            if(msg != null){
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = (Integer) catalogue.get(title);
                if(price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {block();}
        }
    }
    }

