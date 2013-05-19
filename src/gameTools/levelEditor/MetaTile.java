package gameTools.levelEditor;

import java.io.FileWriter;
import java.io.IOException;

import utilities.ByteFormatter;

public class MetaTile {

/*
; MetaTile Format: ( 8 bytes in total)
; 4 bytes to represent which 4 bytes are in the tile
; 1 byte to indicate OAM color.
; 1 byte to indicate the type of tile.
; 2 bytes unused.  Probably be an address to some special activity
; --------------------------------------------------------------

Example:
MetaTileSet1:
.byt $03,$04,$13,$14, $01,$00,$00,$00 ;MetaTile0:
.byt $05,$06,$13,$14, $01,$00,$00,$00 ;MetaTile1:
*/
	byte _topLeftTile;
	byte _topRightTile;
	byte _bottomLeftTile;
	byte _bottomRightTile;
	byte _oam; // 0-3
	int _objectType;
	int _unused1;
	int _unused2;
	
	public MetaTile(int topLeft, int bottomLeft, int topRight, int bottomRight,   int oam, int objectType){
		_topLeftTile = (byte)(topLeft & 0xFF);
		_bottomLeftTile = (byte)(bottomLeft & 0xFF);
		_topRightTile = (byte)(topRight & 0xFF);
		_bottomRightTile = (byte)(bottomRight & 0xFF);
		_oam = (byte)(oam & 0xFF);
		_objectType = objectType;
		_unused1 = 0;
		_unused2 = 0;	
	}
	public String toString() {
		return  ".byt"
		+ " $" + ByteFormatter.formatByte(_topLeftTile)
		+ ",$" + ByteFormatter.formatByte(_bottomLeftTile)
		+ ",$" + ByteFormatter.formatByte(_topRightTile)
		+ ",$" + ByteFormatter.formatByte(_bottomRightTile)
		+ "  "
		+ ",$" + ByteFormatter.formatByte(_oam)
		+ ",$" + ByteFormatter.formatByte((byte)_objectType)
		+ ",$" + ByteFormatter.formatByte((byte)_unused1)
		+ ",$" + ByteFormatter.formatByte((byte)_unused2);
	}
	public boolean writeToFile(FileWriter wr) throws IOException {
		String cr = System.getProperty("line.separator");
		wr.write(".byt"
				+ " $" + ByteFormatter.formatByte(_topLeftTile)
				+ ",$" + ByteFormatter.formatByte(_bottomLeftTile)
				+ ",$" + ByteFormatter.formatByte(_topRightTile)
				+ ",$" + ByteFormatter.formatByte(_bottomRightTile)
				+ "  "
				+ ",$" + ByteFormatter.formatByte(_oam)
				+ ",$" + ByteFormatter.formatByte((byte)_objectType)
				+ ",$" + ByteFormatter.formatByte((byte)_unused1)
				+ ",$" + ByteFormatter.formatByte((byte)_unused2)
				+ cr);
				
		return false;
	}
}
