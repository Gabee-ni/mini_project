package chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import domain.GameRoom;
import domain.Player;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatService { 
	ChatRequest chatRequest;
	private static final Gson gson = new Gson();
	
	public void handle(String msg, Player player, GameRoom room) {
		chatRequest = parseChat(msg, player);

		System.out.println("요청파싱결과 : "+msg + "player: "+ player + "room : "+ room);
		//메시지 유효성 검사
		if (!chatRequest.isChat(chatRequest.getType())) return;
		if (!chatRequest.isValid(chatRequest.getKind())) return;

		String jsonString = buildChatJsonString(chatRequest);
        room.broadcast(jsonString);
	}
	 
	 //요청 매시지 파싱
	 public ChatRequest parseChat(String msg, Player player) {
		 JsonObject jsonObject = JsonParser.parseString(msg).getAsJsonObject();
		 JsonObject payload = jsonObject.getAsJsonObject("payload");
		 String type = jsonObject.get("type").getAsString();
		 String kind = payload.get("kind").getAsString();
		 String content = payload.get("content").getAsString();
		 
		 return new ChatRequest(type, kind, content, player);
	 }
	 
	 //응답 객체로 파싱
	 public static String buildChatJsonString(ChatRequest chatRequest) {
	
		JsonObject root = new JsonObject();
		root.addProperty("type", chatRequest.getType());
		
		JsonObject payload = new JsonObject();
		payload.addProperty("kind", chatRequest.getKind());
		payload.addProperty("content", chatRequest.getContent());
		payload.addProperty("sender", chatRequest.getPlayer().getUserId());
		root.add("payload", payload);
		System.out.println("파싱 결과 : "+root);
		
		return gson.toJson(root);
	        
	}
	 
}
