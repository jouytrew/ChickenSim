/**
 * Created by Benjamin on 9/18/2017.
 */
public interface MoveHistoryIntf {
    Boolean previousOpponentMove(Boolean state);
    Boolean hasOpponentDef(Boolean state);

    Boolean didOpponentDefTwiceInRow(Boolean state);
}