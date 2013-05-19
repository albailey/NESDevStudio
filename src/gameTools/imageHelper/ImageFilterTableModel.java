package gameTools.imageHelper;



import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class ImageFilterTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 5722209854619320401L;

	private Vector<ImageFilter> _filterList = new Vector<ImageFilter>();
	
	static String columnNames[] = { "Enabled", "Filter", "Settings" };

	public ImageFilterTableModel() {
		super();
	}
	
	public int getRowCount() {
		return _filterList.size();
	}
	public int getColumnCount() {
		return columnNames.length;
	}
	
	public Object getValueAt(int row, int column) {
		if(row<0 || column < 0 || column>=getColumnCount() || row >= getRowCount()){
			return null;
		}
		return _filterList.get(row).getColumnValue(column);
	}
	  
	public void addFilter(ImageFilter filter){
		_filterList.add(filter);
		fireTableStructureChanged();
	}
	
	protected Vector<ImageFilter>  getFilters() {
		return _filterList;		
	}
	
	public Class<?> getColumnClass(int column) {
	    return (getValueAt(0, column).getClass());
	 }
	 public String	getColumnName(int column)  {
		 return columnNames[column];
	 }
		
	public boolean isCellEditable(int row, int column) {
		return (column == 0);
	}
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)  {
		if(_filterList.get(rowIndex).setColumnData(columnIndex, aValue)) {
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
	
	public void removeRow(int row){
		_filterList.remove(row);
		fireTableStructureChanged();
	}
}
