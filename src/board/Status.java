package board;

import piece.GamePiece.PieceType;

/**
 * @author ��â��
 * interface about moving on tile
 */
interface Status {
  public void setActive(boolean tf);
  public void setOnPiece(boolean onPiece);
  public void setOccupyPiece(PieceType pc);
}
