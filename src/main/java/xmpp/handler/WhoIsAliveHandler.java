package xmpp.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.ourgrid.cloud.Rendezvous;
import org.ourgrid.cloud.RendezvousItem;
import org.xmpp.packet.IQ;

public class WhoIsAliveHandler extends AbstractQueryHandler {

    final static String NAMESPACE = "http://fogbowcloud.org/rendezvous/whoisalive";
    private Rendezvous rendezvous;

    public WhoIsAliveHandler(Rendezvous rendezvous) {
        super(NAMESPACE);
        this.rendezvous = rendezvous;
    }

    public IQ handle(IQ iq) {
        List<RendezvousItem> aliveIds = rendezvous.whoIsAlive();
        return createResponse(iq, aliveIds);
    }

    private IQ createResponse(IQ iq, List<RendezvousItem> aliveIds) {
        IQ resultIQ = IQ.createResultIQ(iq);

        Element queryElement = resultIQ.getElement().addElement("query",
                NAMESPACE);
        for (RendezvousItem rendezvouItem : aliveIds) {
            Element itemEl = queryElement.addElement("item");
            itemEl.addAttribute("id", rendezvouItem.getResourcesInfo().getId());
            
            Element statusEl = itemEl.addElement("status");
            statusEl.addElement("cpu-idle").setText(
                    rendezvouItem.getResourcesInfo().getCpuIdle());
            statusEl.addElement("cpu-inuse").setText(
                    rendezvouItem.getResourcesInfo().getCpuInUse());
            statusEl.addElement("mem-idle").setText(
                    rendezvouItem.getResourcesInfo().getMemIdle());
            statusEl.addElement("mem-inuse").setText(
                    rendezvouItem.getResourcesInfo().getMemInUse());
            
            //FIXME I think it isn't better place to put this. Maybe RendezvousItem 
            SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
            Date date = new Date(rendezvouItem.getLastTime());
            
            statusEl.addElement("updated").setText(
                    String.valueOf(dateFormatISO8601.format(date))); 
        }
        return resultIQ;
    }
}
