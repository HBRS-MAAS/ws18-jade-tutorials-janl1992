package maas.tutorials;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent {
	private String targetBookTitle;
	private AID[] sellerAgents;
	private static int iMaxBook = 3;
	protected void setup() {
		System.out.println("Hello! BookBuyerAgent" + getAID().getName() + "is ready - waiting 10 seconds to start behaviour");
		Object[] oArguments = getArguments();
		if (oArguments != null && oArguments.length > 0) {
			RequestPerformer rpRequestPerformer = new RequestPerformer();
			targetBookTitle = (String) oArguments[0];
			System.out.println("Trying to buy " + targetBookTitle);
			addBehaviour(new TickerBehaviour(this, 10000) {
				@Override
				protected void onTick() {
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("book-selling");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template);
						sellerAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i){
							sellerAgents[i] = result[i].getName();
						}
					} catch (FIPAException e) {
						e.printStackTrace();
					}
					myAgent.addBehaviour(rpRequestPerformer);
				}
			});
		} else {
			System.out.println("No book title specified");
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println("BookBuyerAgent" + getAID().getName() + "terminating");
	}

	private class RequestPerformer extends Behaviour {
		private AID bestSeller;
		private int bestPrice;
		private int repliesCnt = 0;
		private MessageTemplate mt;
		private int step = 0;
		private int iPurchasedBookCounter = 0;
		@Override
		public void action() {
			switch (step) {
				case 0:
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for(int i = 0; i < sellerAgents.length; ++i){
						cfp.addReceiver(sellerAgents[i]);
					}
					cfp.setContent(targetBookTitle);
					cfp.setConversationId("book-trade");
					cfp.setReplyWith("cfp" + System.currentTimeMillis());
					myAgent.send(cfp);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1:
					ACLMessage reply = myAgent.receive(mt);
					if(reply != null){
						if(reply.getPerformative() == ACLMessage.PROPOSE){
							int price = Integer.parseInt(reply.getContent());
							if(bestSeller == null || price < bestPrice){
								bestPrice = price;
								bestSeller = reply.getSender();
							}
						}
						repliesCnt++;
						if(repliesCnt >= sellerAgents.length){
							step = 2;
						}
					}
					else {block();}
					break;
				case 2:
					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					order.addReceiver(bestSeller);
					order.setContent(targetBookTitle);
					order.setConversationId("book-trade");
					order.setReplyWith("order" + System.currentTimeMillis());
					myAgent.send(order);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),MessageTemplate.MatchInReplyTo(order.getReplyWith()));
					step = 3;
					break;
				case 3:
					reply = myAgent.receive(mt);
					if(reply != null){
						if(reply.getPerformative() == ACLMessage.INFORM){
							System.out.println(targetBookTitle + " successfully purchased");
							System.out.println("Price = " + bestPrice);
							iPurchasedBookCounter++;
							if (iPurchasedBookCounter == iMaxBook) {
								System.out.println("BookBuyerAgent" + getAID().getName() + "succesfully purchased " + iMaxBook + "books - terminating");
								myAgent.doDelete();
							} else{
								step = 0;
								break;
							}
						}
						step = 4;
					}
					else {block();}
					break;
			}
		}

		@Override
		public boolean done() {
			return ((step == 2 && bestSeller == null) || step == 4 && iMaxBook == 3);
		}
	}

}

