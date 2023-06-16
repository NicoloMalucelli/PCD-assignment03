package ex2;

import com.fasterxml.jackson.databind.ObjectMapper;
import ex2.message.GridMessage;
import ex2.message.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class test {

    private static class ListMessage extends Message{
        List<Integer> list = new LinkedList<>();
        ListMessage(){}

        public List<Integer> getList() {
            return list;
        }

        public void setList(List<Integer> list) {
            this.list = list;
        }
    }

    public static void main(String[] args) throws IOException {
        var mapper = new ObjectMapper();
        ListMessage message = new ListMessage();
        message.setList(List.of(1,2,3,4));

        String json = mapper.writeValueAsString(message);
        System.out.println(json);

        ListMessage joinMessage2 = mapper.readValue(json, ListMessage.class);
        System.out.println(joinMessage2.getList());
    }

}
