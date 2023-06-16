package ex2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import ex2.message.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class ConnectionHandler {

    private static final String EXCHANGE_NAME = "pixelGrid_exchange";
    private final String clientId = String.valueOf(new Random().nextInt());
    private final Channel channel;
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean gridInitialized;

    private final BrushManager brushManager;
    private final PixelGrid pixelGrid;
    private final PixelGridView pixelGridView;


    public static int randomColor() {
        Random rand = new Random();
        return rand.nextInt(256 * 256 * 256);
    }

    public ConnectionHandler(BrushManager brushManager, PixelGrid pixelGrid, PixelGridView pixelGridView) throws IOException, TimeoutException {
        this.brushManager = brushManager;
        this.pixelGrid = pixelGrid;
        this.pixelGridView = pixelGridView;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, EXCHANGE_NAME, "join");
        channel.queueBind(queueName, EXCHANGE_NAME, "leave");
        channel.queueBind(queueName, EXCHANGE_NAME, "newPosition");
        channel.queueBind(queueName, EXCHANGE_NAME, "newColor");
        channel.queueBind(queueName, EXCHANGE_NAME, "newPaintedPixel");
        channel.queueBind(queueName, EXCHANGE_NAME, "initInfo-"+this.clientId);
        channel.queueBind(queueName, EXCHANGE_NAME, "gridRequest-"+this.clientId);
        channel.queueBind(queueName, EXCHANGE_NAME, "gridResponse-"+this.clientId);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            switch (delivery.getEnvelope().getRoutingKey()){
                case "join" -> this.onJoin(mapper.readValue(delivery.getBody(), JoinMessage.class));
                case "leave" -> this.onLeave(mapper.readValue(delivery.getBody(), Message.class));
                case "newPosition" -> this.onNewPosition(mapper.readValue(delivery.getBody(), MovedMessage.class));
                case "newColor" -> this.onNewColor(mapper.readValue(delivery.getBody(), ColorChangedMessage.class));
                case "newPaintedPixel" -> this.onNewPaintedPixel(mapper.readValue(delivery.getBody(), PaintMessage.class));
            }
            if(delivery.getEnvelope().getRoutingKey().equals("initInfo-"+this.clientId)){
                this.onInitInfo(mapper.readValue(delivery.getBody(), InitMessage.class));
            }else if(delivery.getEnvelope().getRoutingKey().equals("gridRequest-"+this.clientId)){
                this.onGridRequest(mapper.readValue(delivery.getBody(), Message.class));
            }else if(delivery.getEnvelope().getRoutingKey().equals("gridResponse-"+this.clientId)){
                this.onGridResponse(mapper.readValue(delivery.getBody(), GridMessage.class));
            }

            pixelGridView.refresh();
        };

        channel.basicConsume(queueName, true, deliverCallback, t -> {});
    }

    public void connect() throws IOException {
        final JoinMessage join = new JoinMessage(this.clientId, randomColor());
        channel.basicPublish(EXCHANGE_NAME, "join", null, mapper.writeValueAsBytes(join));
    }

    public void disconnect() throws IOException {
        final Message disconnect = new Message(this.clientId);
        channel.basicPublish(EXCHANGE_NAME, "leave", null, mapper.writeValueAsBytes(disconnect));
    }

    public void sendNewPosition(int x, int y) throws IOException {
        final MovedMessage message = new MovedMessage(this.clientId, x, y);
        channel.basicPublish(EXCHANGE_NAME, "newPosition", null, mapper.writeValueAsBytes(message));
    }

    public void sendPaintedPixel(int x, int y) throws IOException {
        final PaintMessage message = new PaintMessage(this.clientId, x, y);
        channel.basicPublish(EXCHANGE_NAME, "newPaintedPixel", null, mapper.writeValueAsBytes(message));
    }

    public void sendNewColor(int color) throws IOException {
        final ColorChangedMessage message = new ColorChangedMessage(this.clientId, color);
        channel.basicPublish(EXCHANGE_NAME, "newColor", null, mapper.writeValueAsBytes(message));
    }

    private void onJoin(JoinMessage joinMessage) throws IOException {
        final BrushManager.Brush newBrush = new BrushManager.Brush(0, 0, joinMessage.getColor(), joinMessage.getId());
        brushManager.addBrush(newBrush);

        if(!joinMessage.getId().equals(this.clientId)) {
            BrushManager.Brush myBrush = brushManager.getBrush(this.clientId);
            final InitMessage initMessage = new InitMessage(this.clientId, myBrush.getX(), myBrush.getY(), myBrush.getColor());
            channel.basicPublish(EXCHANGE_NAME, "initInfo-"+joinMessage.getId(), null, mapper.writeValueAsBytes(initMessage));
        }
    }

    private void onLeave(Message message) {
        brushManager.removeBrush(brushManager.getBrush(message.getId()));
    }

    private void onNewPosition(MovedMessage message) {
        brushManager.getBrush(message.getId()).updatePosition(message.getX(), message.getY());
    }

    private void onNewPaintedPixel(PaintMessage message) {
        pixelGrid.set(message.getX(), message.getY(), brushManager.getBrush(message.getId()).getColor());
    }

    private void onNewColor(ColorChangedMessage message) {
        brushManager.getBrush(message.getId()).setColor(message.getColor());
    }

    private void onInitInfo(InitMessage message) throws IOException {
        if(!gridInitialized){
            gridInitialized = true;
            final Message gridRequest = new Message(this.clientId);
            channel.basicPublish(EXCHANGE_NAME, "gridRequest-"+message.getId(), null, mapper.writeValueAsBytes(gridRequest));
        }
        final BrushManager.Brush newBrush = new BrushManager.Brush(message.getX(), message.getY(), message.getColor(), message.getId());
        brushManager.addBrush(newBrush);
    }

    private void onGridRequest(Message message) throws IOException {
        final GridMessage initMessage = new GridMessage(this.clientId, pixelGrid.toList());
        channel.basicPublish(EXCHANGE_NAME, "gridResponse-"+message.getId(), null, mapper.writeValueAsBytes(initMessage));
    }

    private void onGridResponse(GridMessage message) throws IOException {
        message.getPixels().forEach(p -> pixelGrid.set(p.getX(), p.getY(), p.getColor()));
    }
}
