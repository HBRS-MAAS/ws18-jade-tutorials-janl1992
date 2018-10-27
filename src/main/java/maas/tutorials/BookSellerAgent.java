package maas.tutorials;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class BookSellerAgent extends Agent {
    private Hashtable catalogue;
    private long lCurrentTime;

    protected void setup() {
        catalogue = new Hashtable();
        Object[] oArguments = getArguments();
        lCurrentTime = System.currentTimeMillis();
        String[] sSplit;
        for (Object oArgument:
             oArguments) {
            String oArgument1 = (String) oArgument;
            sSplit = oArgument1.split(" ");
            Book b = new Book(sSplit[0], Integer.valueOf(sSplit[1]), Integer.valueOf(sSplit[2]));
            updateCatalogue(b);

        }
        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                long lDiff = System.currentTimeMillis() - lCurrentTime;
                System.out.println(lDiff);
                if(lDiff > 30000){
                    ACLMessage aclmShutDownMessage = new ACLMessage(ACLMessage.REQUEST);
                    Codec codec = new SLCodec();
                    myAgent.getContentManager().registerLanguage(codec);
                    myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
                    aclmShutDownMessage.addReceiver(myAgent.getAMS());
                    aclmShutDownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    aclmShutDownMessage.setOntology(JADEManagementOntology.getInstance().getName());
                    try {
                        myAgent.getContentManager().fillContent(aclmShutDownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
                        myAgent.send(aclmShutDownMessage);
                    }
                    catch (Exception e) {
                        System.out.println(e.getStackTrace());
                    }
                }
            }
        });
        addBehaviour(new OfferRequestServer());
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
//            if (lDiff > 60000)
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
//                for (Object oBook:
//                     catalogue.values()) {
//                    Book b = (Book) oBook;
//                    if(b.getiQuantity() == 0){
//                        System.out.println(getAID().getName() + " is terminated as quantity of " + b.getsBookTitle() + " is 0");
//                        doDelete();
//                        break;
//                    }
//
//                }
            }
        }
    }

    private void refuseoffer(ACLMessage reply) {
        reply.setPerformative(ACLMessage.REFUSE);
        reply.setContent("not-available");
    }
}

