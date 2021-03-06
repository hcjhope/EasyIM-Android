package me.xiezefan.easyim.mvp.new_friend;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import me.xiezefan.easyim.Application;
import me.xiezefan.easyim.R;
import me.xiezefan.easyim.dao.Friend;
import me.xiezefan.easyim.dao.FriendRequest;
import me.xiezefan.easyim.event.model.ProcessEvent;
import me.xiezefan.easyim.mvp.activity.BaseActivity;
import me.xiezefan.easyim.mvp.activity.FriendsSquareActivity;
import me.xiezefan.easyim.mvp.friend_home.FriendHomeActivity;

import static android.support.v7.widget.RecyclerView.OnScrollListener;


public class NewFriendFragment extends Fragment implements NewFriendView, NewFriendAdapter.OnItemClickListener {
    @InjectView(R.id.rvList)
    RecyclerView rvList;
    @Inject
    NewFriendPresenter newFriendPresenter;

    private NewFriendAdapter newFriendAdapter;
    private LinearLayoutManager newFriendLayoutManager;
    private MaterialDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        // inject
        ButterKnife.inject(this, view);
        ((Application) getActivity().getApplication()).getApplicationGraph().inject(this);


        initView();
        initData();
        return view;
    }

    private void initView() {
        // init presenter
        newFriendPresenter.setNewFriendView(this);

        // init toolbar
        BaseActivity _activity = (BaseActivity) getActivity();
        _activity.getToolbar().setTitle("新的朋友");

        // init dialog
        progressDialog = new MaterialDialog.Builder(getActivity())
                .content("添加好友中")
                .progress(true, 0)
                .build();

        // init list
        newFriendAdapter = new NewFriendAdapter(getActivity());
        newFriendAdapter.setOnItemClickListener(this);
        newFriendLayoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(newFriendLayoutManager);
        rvList.setAdapter(newFriendAdapter);
        rvList.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = newFriendLayoutManager.findLastVisibleItemPosition();
                lastVisibleItem = lastVisibleItem - NewFriendAdapter.FRIEND_REQUEST_ITEM_COUNT_OFFSET;
                newFriendPresenter.onLastVisibleItemChange(lastVisibleItem);
            }
        });

    }

    private void initData() {
        // init data
        newFriendPresenter.initFriendRequests();
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this, 5);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyFriendRequestsChange(List<FriendRequest> dataSet, int start, int row) {
        newFriendAdapter.updateFriendRequests(dataSet);
        newFriendAdapter.notifyDataSetChanged();

    }

    @Override
    public void startFriendHomeActivity(Friend friend) {
        Intent intent = new Intent(getActivity(), FriendHomeActivity.class);
        intent.putExtra(FriendHomeActivity.KEY_FRIEND_ID, friend.getUid());
        getActivity().startActivity(intent);
    }

    @Override
    public void onFriendSquareItemClick() {
        getActivity().startActivity(new Intent(getActivity(), FriendsSquareActivity.class));
    }

    @Override
    public void onFriendSearchItemClick() {
        getActivity().startActivity(new Intent(getActivity(), FriendsSquareActivity.class));
    }

    @Override
    public void onFriendRequestItemClick(int position) {
        newFriendPresenter.onFriendRequestItemClick(position);
    }

    /*---- Events ----*/



    /**
     * 请求发送完成事件
     * @param event
     */
    public void onEventMainThread(ProcessEvent event) {
        if (event == ProcessEvent.FRIEND_REQUEST_SUCCESS_EVENT) {
            progressDialog.hide();
        }
    }

    /**
     * 接收到新的添加好友消息
     * @param newFriend
     */
    public void onEvent(FriendRequest newFriend) {
        newFriendPresenter.onReceiveNewFriendRequest(newFriend);
        EventBus.getDefault().cancelEventDelivery(newFriend);
    }


}
