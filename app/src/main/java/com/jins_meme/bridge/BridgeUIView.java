package com.jins_meme.bridge;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.Stack;

/**
 * Created by nariakiiwatani on 2017/01/30.
 */

public class BridgeUIView extends RecyclerView {
    private CardLayoutManager mLayoutManager;
    public BridgeUIView(Context context) {
        super(context);
        mLayoutManager = new CardLayoutManager(getContext());
        setLayoutManager(mLayoutManager);
        addItemDecoration(new CardDecoration());
    }

    public void move(int amount) {
        smoothScrollToPosition(getCurrentCenteredItemPosition()+amount);
    }
    public void enter() {
        mLayoutManager.findViewByPosition(getCurrentCenteredItemPosition()).callOnClick();
    }
    public void reset() {
        ((Adapter)getAdapter()).reset();
    }

    private int getCurrentCenteredItemPosition() {
        int first = mLayoutManager.findFirstVisibleItemPosition();
        View firstView = mLayoutManager.findViewByPosition(first);
        if(firstView.getLeft()+firstView.getWidth()/2.f < 0) {
            return first+1;
        }
        return first;
    }

    public interface IResultListener {
        public void onBridgeMenuFinished(int id);
    }

    public static abstract class Adapter<CH extends CardHolder> extends RecyclerView.Adapter<CH> {
        public static final int NO_ID = (int) RecyclerView.NO_ID;
        enum CardFunction {
            BACK, ENTER_MENU, END,
        }
        private Stack<Integer> mHistory = new Stack<Integer>();
        private IResultListener mListener;
        Adapter(IResultListener listener) {
            mListener = listener;
            setHasStableIds(true);
        }
        public abstract CardFunction getCardFunction(int id);
        public abstract int getCardId(int parent_id, int position);
        public abstract CH onCreateCardHolder(ViewGroup parent, int card_type);
        public abstract void onBindCardHolder(CH cardHolder, int id);
        public abstract int getChildCardCount(int parent_id);
        public int getCardType(int id) { return 0; }

        public void reset() {
            mHistory.clear();
            notifyDataSetChanged();
        }

        @Override
        public final CH onCreateViewHolder(ViewGroup parent, int viewType) {
            CH ch = onCreateCardHolder(parent, viewType);
            return ch;
        }
        @Override
        public final int getItemCount() {
            return Integer.MAX_VALUE;
        }
        @Override
        public final long getItemId(int position) {
            return getCardId(getSelectedCardId(), calcCardPosition(position));
        }
        @Override
        public final void onBindViewHolder(CH viewHolder, final int position) {
            onBindCardHolder(viewHolder, (int)getItemId(position));
            final int id = (int)viewHolder.getItemId();
            final CardFunction result = getCardFunction(id);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(result) {
                        case BACK:
                            if(mHistory.empty()) {
                                mListener.onBridgeMenuFinished(NO_ID);
                            }
                            else {
                                mHistory.pop();
                                notifyDataSetChanged();
                            }
                            break;
                        case ENTER_MENU:
                            mHistory.push(id);
                            notifyDataSetChanged();
                            break;
                        case END:
                            mListener.onBridgeMenuFinished(id);
                            break;
                    }
                }
            });
        }
        private int calcCardPosition(int position) { return position%getChildCardCount(getSelectedCardId()); }
        private int getSelectedCardId() { return mHistory.empty()?NO_ID: mHistory.peek(); }
    }
    public static class CardHolder extends RecyclerView.ViewHolder {

        public CardHolder(View itemView) {
            super(itemView);
        }
    }
    private class CardDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            outRect.set(30,30,30,30);
        }
    }
    private class CardLayoutManager extends LinearLayoutManager {
        public CardLayoutManager(Context context) {
            super(context, LinearLayoutManager.HORIZONTAL, false);
        }

        @Override
        public void onLayoutCompleted(State state) {
            super.onLayoutCompleted(state);
            scrollToPosition(getAdapter().getItemCount()/2);
        }
    }
}
