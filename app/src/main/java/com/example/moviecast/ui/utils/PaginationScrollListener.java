package com.example.moviecast.ui.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * PaginationScrollListener - подгрузка следующей страницы при прокрутке списка.
 * Вызывает loadMoreItems(), когда пользователь доскроллил до конца.
 */
public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {
    
    private GridLayoutManager layoutManager;
    
    public PaginationScrollListener(GridLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }
    
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        
        // Достигли конца списка - подгружаем ещё
        if (!isLoading() && hasMorePages()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems();
            }
        }
    }
    
    protected abstract void loadMoreItems();
    protected abstract boolean isLoading();
    protected abstract boolean hasMorePages();
}
