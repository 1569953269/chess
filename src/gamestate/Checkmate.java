package gamestate;

import board.ChessBoard;
import board.Status.TEAM;
import chess.ChessGui;
import gamestate.GameController;
import piece.Bishop;
import piece.GamePiece;
import piece.GamePiece.Color;
import piece.GamePiece.PieceType;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Position;
import piece.Queen;
import piece.Rook;

/**
 * 
 * @author Yeoilgoo
 *
 * @since 2018-06-02
 */

public class Checkmate {
	
	Check check;
  
  King[] king;
  Queen[] queen;
  Knight[][] knight;
  Bishop[][] bishop;
  Rook[][] rook;
  Pawn[][] pawn;
  Position[] aw; // availableWay
	
  int kingX, kingY;
	Color myColor = null;
	TEAM myTeam = null, teammateTeam = null, oppositeTeam1 = null, oppositeTeam2 = null; //opcolor2 is the next enemy color
	int teammate = 0, opposite1 = 0, opposite2 = 0;
	int enemyPieceNum = 0; // enemy piece numbers can attack my king
	GamePiece[] enemyPiece = new GamePiece[30];
	int targetX = 0, targetY = 0, directionX = 0, directionY = 0;
	
	public Checkmate() { 
  	this.king = ChessGui.b.king;
    this.queen = ChessGui.b.queen;
    this.knight = ChessGui.b.knight;
    this.bishop = ChessGui.b.bishop;
    this.rook = ChessGui.b.rook;
    this.pawn = ChessGui.b.pawn;
  }
	
	public void isCheckmate() {
		for(int playerNum = 0; playerNum < 4; playerNum++) {
			if(GameController.checkFlag[playerNum] == 0) {
  			GameController.checkmateFlag[playerNum] = 0;
  			break;
  		}
			
			else {
				if(king[playerNum].getCanMoves().length != 0) {
  				GameController.checkmateFlag[playerNum] = 0;
  				break;
				}
				
				else {
					myColor = king[playerNum].getColor();
  				myTeam = GameController.colorToTeam(myColor);
  				
  				switch(myColor) {
  				case WHITE:
  					oppositeTeam1 = TEAM.GREEN;
  					oppositeTeam2 = TEAM.RED;
  					teammateTeam = TEAM.BLACK;
  					break;
  				case BLACK:
  					oppositeTeam1 = TEAM.RED;
  					oppositeTeam2 = TEAM.GREEN;
  					teammateTeam = TEAM.WHITE;
  					break;
  				case RED:
  					oppositeTeam1 = TEAM.WHITE;
  					oppositeTeam2 = TEAM.BLACK;
  					teammateTeam = TEAM.GREEN;
  					break;
  				case GREEN:
  					oppositeTeam1 = TEAM.BLACK;
  					oppositeTeam2 = TEAM.WHITE;
  					teammateTeam = TEAM.RED;
  				}
					
  				opposite1 = ChessBoard.cvtTeam(oppositeTeam1);
  				opposite2 = ChessBoard.cvtTeam(oppositeTeam2);
  				teammate = ChessBoard.cvtTeam(teammateTeam);
  				kingX = king[playerNum].getPosition().getX();
  				kingY = king[playerNum].getPosition().getY();
  				
					enemyCanAttackKing(opposite1);
					enemyCanAttackKing(opposite2);
					
					/*
					 * enemyPieceNum means number of opposite pieces can attack player's king.
					 * if enemyPieceNum > 2, it always checkmate.
					 * if enemyPieceNum = 2, there are three cases.
					 * if enemyPieceNum = 1, there are two cases.
					 */
					if(enemyPieceNum > 2) {
						GameController.checkmateFlag[playerNum] = 1;
						break;
					}
					
					else if(enemyPieceNum == 2) {
						
						/*
						 * if all of piece team is op2, always checkmate.
						 */
						if(enemyPiece[0].getColor() == GameController.teamToColor(opposite2) &&
								enemyPiece[1].getColor() == GameController.teamToColor(opposite2)) {
							GameController.checkmateFlag[playerNum] = 1;
							break;
						}
						
						/*
						 * if enemypiece[0] team is op1, enemypiece[1] is op2.
						 * if player can delte op2 and team can delte op1, it is not checkmate.
						 */
						else if(enemyPiece[0].getColor() == GameController.teamToColor(opposite1) &&
								enemyPiece[1].getColor() == GameController.teamToColor(opposite2)) {
							GameController.checkmateFlag[playerNum] = 1;
							
							if(enemyPiece[0].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
								if(enemyPiece[1].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
									enemyIsKnightOrPawn(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
									GameController.checkmateFlag[playerNum] = 1;
									enemyIsKnightOrPawn(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
								}
								
								else {
									enemyIsKnightOrPawn(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
									GameController.checkmateFlag[playerNum] = 1;
									pieceWay(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
								}
							}
							
							else {
								if(enemyPiece[1].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
									pieceWay(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
									GameController.checkmateFlag[playerNum] = 1;
									enemyIsKnightOrPawn(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
								}
								else {
									pieceWay(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
									GameController.checkmateFlag[playerNum] = 1;
									pieceWay(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 1) {
										break;
									}
								}
							}
						}
						
						/*
						 * if all enemy is op1, the player delete enemypieces isn't matter. but they must delete all pieces.
						 */
						else {
							int[] playerFlag = {0, 0};
							int[] teamFlag = {0, 0};
							GameController.checkmateFlag[playerNum] = 1;

							//case1
							if(enemyPiece[0].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
								if(enemyPiece[1].getPieceType() == PieceType.KNIGHT || enemyPiece[1].getPieceType() == PieceType.PAWN) {
									enemyIsKnightOrPawn(0, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[0] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									enemyIsKnightOrPawn(1, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[1] = 1;
									}
								}
								
								else {
									enemyIsKnightOrPawn(0, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[0] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									pieceWay(1, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[1] = 1;
									}
								}
							}
							
							else {
								if(enemyPiece[1].getPieceType() == PieceType.KNIGHT || enemyPiece[1].getPieceType() == PieceType.PAWN) {
									pieceWay(0, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[0] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									enemyIsKnightOrPawn(1, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[1] = 1;
									}
								}
								
								else {
									pieceWay(0, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[0] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									pieceWay(1, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[1] = 1;
									}
								}
							}
							
							//case2
							
							if(enemyPiece[0].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
								if(enemyPiece[1].getPieceType() == PieceType.KNIGHT || enemyPiece[1].getPieceType() == PieceType.PAWN) {
									enemyIsKnightOrPawn(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[1] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									enemyIsKnightOrPawn(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[0] = 1;
									}
								}
								
								else {
									enemyIsKnightOrPawn(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[1] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									pieceWay(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[0] = 1;
									}
								}
							}
							
							else {
								if(enemyPiece[1].getPieceType() == PieceType.KNIGHT || enemyPiece[1].getPieceType() == PieceType.PAWN) {
									pieceWay(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[1] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									enemyIsKnightOrPawn(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[0] = 1;
									}
								}
								
								else {
									pieceWay(1, playerNum, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										playerFlag[1] = 1;
									}
									GameController.checkmateFlag[playerNum] = 1;
									pieceWay(0, teammate, playerNum);
									if(GameController.checkmateFlag[playerNum] == 0) {
										teamFlag[0] = 1;
									}
								}
							}
							
							if((playerFlag[0] > 0 && teamFlag[1] > 0) || (playerFlag[1] > 0 && teamFlag[0] > 0)) {
								GameController.checkmateFlag[playerNum] = 0;
							}
							else {
								GameController.checkmateFlag[playerNum] = 1;
							}
						}
					}
					/*
					 * if PieceNum is one, there are two cases.
					 * first, if piece[0] = op1, the player delete enemypieces isn't matter.
					 * second, if piece[0] = op2, player must delte enemypieces.
					 */
					else { //case PieceNum = 1
						if(enemyPiece[0].getColor() == GameController.teamToColor(opposite1)) {
							GameController.checkmateFlag[playerNum] = 1;
							if(enemyPiece[0].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
								enemyIsKnightOrPawn(0, playerNum, playerNum);
								enemyIsKnightOrPawn(0, teammate, playerNum);
							}
							
							else {
								pieceWay(0, playerNum, playerNum);
								pieceWay(0, teammate, playerNum);
							}
						}
						
						else {
							if(enemyPiece[0].getPieceType() == PieceType.KNIGHT || enemyPiece[0].getPieceType() == PieceType.PAWN) {
								enemyIsKnightOrPawn(0, playerNum, playerNum);
							}
							
							else {
								pieceWay(0, playerNum, playerNum);
							}
						}
					}
				}
			}
		}
		return;
	}
	
	private void enemyCanAttackKing(int opposite) {
		aw = queen[opposite].getCanMoves();
		for(int i = 0; i < aw.length; i++) {
			if(aw[i].getX() == kingX && aw[i].getY() == kingY) {
				enemyPiece[enemyPieceNum] = queen[opposite];
				enemyPieceNum++;
			}
		}
		
		for(int i = 0; i < bishop[opposite].length; i++) {
			aw = bishop[opposite][i].getCanMoves();
			for(int k = 0; k < aw.length; k++) {
				if(aw[k].getX() == kingX && aw[k].getY() == kingY) {
					enemyPiece[enemyPieceNum] = bishop[opposite][i];
					enemyPieceNum++;
				}
			}
		}
		
		for(int i = 0; i < rook[opposite].length; i++) {
			aw = rook[opposite][i].getCanMoves();
			for(int k = 0; k < aw.length; k++) {
				if(aw[k].getX() == kingX && aw[k].getY() == kingY) {
					enemyPiece[enemyPieceNum] = rook[opposite][i];
					enemyPieceNum++;
				}
			}
		}
		
		for(int i = 0; i < knight[opposite].length; i++) {
			aw = knight[opposite][i].getCanMoves();
			for(int k = 0; k < aw.length; k++) {
				if(aw[k].getX() == kingX && aw[k].getY() == kingY) {
					enemyPiece[enemyPieceNum] = knight[opposite][i];
					enemyPieceNum++;
				}
			}
		}
		
		for(int i = 0; i < pawn[opposite].length; i++) {
			aw = pawn[opposite][i].getCanMoves();
			for(int k = 0; k < aw.length; k++) {
				if(aw[k].getX() == kingX && aw[k].getY() == kingY) {
					enemyPiece[enemyPieceNum] = pawn[opposite][i];
					enemyPieceNum++;
				}
			}
		}
		return;
	}
	
  private void pieceWay(int pieceNum, int oneOfTeam, int player) {
  	targetX = enemyPiece[pieceNum].getPosition().getX();
		targetY = enemyPiece[pieceNum].getPosition().getY();
		directionX = targetX - kingX;
		directionY = targetY - kingY;
  	
		//can delete enemy
		aw = queen[oneOfTeam].getCanMoves();
		for(int i = 0; i < aw.length; i++) {
			if(aw[i].getY() == targetY && aw[i].getX() == targetX) {
				GameController.checkmateFlag[player] = 0;
				break;
			}
		}
		
		for(int i = 0; i < bishop[oneOfTeam].length; i++) {
			aw = bishop[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		for(int i = 0; i < rook[oneOfTeam].length; i++) {
			aw = rook[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		for(int i = 0; i < knight[oneOfTeam].length; i++) {
			aw = knight[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		for(int i = 0; i < pawn[oneOfTeam].length; i++) {
			aw = pawn[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		//can block enemy's path
		if(targetX == kingX) {
			aw = queen[oneOfTeam].getCanMoves();
			for(int i = 0; i < aw.length; i++) {
				for(int j = 0; j < Math.abs(directionY); j++) {
					if(aw[i].getX() == targetX && aw[i].getY() == targetY + (j * directionY/Math.abs(directionY))){
						GameController.checkmateFlag[player] = 0;
						break;
					}
				}
			}
			
			for(int i = 0; i < bishop[oneOfTeam].length; i++) {
				aw = bishop[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX && aw[j].getY() == targetY + (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < rook[oneOfTeam].length; i++) {
				aw = rook[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX && aw[j].getY() == targetY + (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < knight[oneOfTeam].length; i++) {
				aw = knight[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX && aw[j].getY() == targetY + (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < pawn[oneOfTeam].length; i++) {
				aw = pawn[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX && aw[j].getY() == targetY + (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
		}
		
		else if(targetY == kingY) {
			aw = queen[oneOfTeam].getCanMoves();
			for(int i = 0; i < aw.length; i++) {
				for(int j = 0; j < Math.abs(directionY); j++) {
					if(aw[i].getX() == targetX  + (j * directionX/Math.abs(directionX))&& aw[i].getY() == targetY){
						GameController.checkmateFlag[player] = 0;
						break;
					}
				}
			}
			
			for(int i = 0; i < bishop[oneOfTeam].length; i++) {
				aw = bishop[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < rook[oneOfTeam].length; i++) {
				aw = rook[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < knight[oneOfTeam].length; i++) {
				aw = knight[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < pawn[oneOfTeam].length; i++) {
				aw = pawn[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
		}
		
		else {
			aw = queen[oneOfTeam].getCanMoves();
			for(int i = 0; i < aw.length; i++) {
				for(int j = 0; j < Math.abs(directionY); j++) {
					if(aw[i].getX() == targetX  + (j * directionX/Math.abs(directionX))&& aw[i].getY() == targetY+ (j * directionY/Math.abs(directionY))){
						GameController.checkmateFlag[player] = 0;
						break;
					}
				}
			}
			
			for(int i = 0; i < bishop[oneOfTeam].length; i++) {
				aw = bishop[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY+ (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < rook[oneOfTeam].length; i++) {
				aw = rook[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY+ (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < knight[oneOfTeam].length; i++) {
				aw = knight[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY+ (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < pawn[oneOfTeam].length; i++) {
				aw = pawn[oneOfTeam][i].getCanMoves();
				for(int j = 0; j < aw.length; j++) {
					for(int k = 0; k < Math.abs(directionY); k++) {
						if(aw[j].getX() == targetX + (k * directionX/Math.abs(directionX)) && aw[j].getY() == targetY+ (k * directionY/Math.abs(directionY))){
							GameController.checkmateFlag[player] = 0;
							break;
						}
					}
				}
			}
		}
	return;
  }
	
  /*
   * if enemypiece is pawn of knight, we can't bloc it's path. we must delte it.
   */
  private void enemyIsKnightOrPawn(int pieceNum, int oneOfTeam, int player) {
  	targetX = enemyPiece[pieceNum].getPosition().getX();
		targetY = enemyPiece[pieceNum].getPosition().getY();
		directionX = targetX - kingX;
		directionY = targetY - kingY;
  	
		//can delete enemy
		aw = queen[oneOfTeam].getCanMoves();
		for(int i = 0; i < aw.length; i++) {
			if(aw[i].getY() == targetY && aw[i].getX() == targetX) {
				GameController.checkmateFlag[player] = 0;
				break;
			}
		}
		
		for(int i = 0; i < bishop[oneOfTeam].length; i++) {
			aw = bishop[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		for(int i = 0; i < rook[oneOfTeam].length; i++) {
			aw = rook[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		for(int i = 0; i < knight[oneOfTeam].length; i++) {
			aw = knight[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
		
		for(int i = 0; i < pawn[oneOfTeam].length; i++) {
			aw = pawn[oneOfTeam][i].getCanMoves();
			for(int j = 0; j < aw.length; j++) {
				if(aw[j].getY() == targetY && aw[j].getX() == targetX) {
					GameController.checkmateFlag[player] = 0;
					break;
				}
			}
		}
  	return;
  }
}