package com.playground.notification.app.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

/**
 * An abstract {@link RecyclerView.Adapter} that supports action-mode.
 * <p/>
 * <p/>
 * See: <a href="http://databasefaq.com/index.php/answer/19065/android-android-fragments-recyclerview-android-actionmode-problems-with-implementing-contextual-action-mode-in-recyclerview-fragment">Problems
 * with implementing contextual action mode in recyclerview fragment</a>
 *
 * @author Xinyue Zhao
 */
public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	@SuppressWarnings("unused")
	private static final String TAG = SelectableAdapter.class.getSimpleName();

	private SparseBooleanArray selectedItems;
	private boolean            mActionMode;

	public SelectableAdapter() {
		selectedItems = new SparseBooleanArray();
	}


	/**
	 * Indicates if the item at position position is selected
	 *
	 * @param position
	 * 		Position of the item to check
	 *
	 * @return true if the item is selected, false otherwise
	 */
	public boolean isSelected( int position ) {
		return getSelectedItems().contains( position );
	}


	/**
	 * Toggle the selection status of the item at a given position
	 *
	 * @param position
	 * 		Position of the item to toggle the selection status for
	 */
	public void toggleSelection( int position ) {
		if( selectedItems.get( position, false ) ) {
			selectedItems.delete( position );
		} else {
			selectedItems.put( position, true );
		}
		notifyItemChanged( position );
	}

	/**
	 * Clear the selection status for all items
	 */
	public void clearSelection() {
		List<Integer> selection = getSelectedItems();
		selectedItems.clear();
		for( Integer i : selection ) {
			notifyItemChanged( i );
		}
	}

	/**
	 * Count the selected items
	 *
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	/**
	 * Indicates the list of selected items
	 *
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<>( selectedItems.size() );
		for( int i = 0; i < selectedItems.size(); ++i ) {
			items.add( selectedItems.keyAt( i ) );
		}
		return items;
	}


	public boolean isActionMode() {
		return mActionMode;
	}

	public void setActionMode( boolean actionMode ) {
		mActionMode = actionMode;
	}
}