package ee.taltech.pony_dash_for_spikes_salvation.packets;

public class PacketGameOver {
    private int playerId;
    private int gameId;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
