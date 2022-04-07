package com.williamnb.readlistenapp.features.chat.users_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.williamnb.readlistenapp.base.BaseFragment;
import com.williamnb.readlistenapp.data.models.User;
import com.williamnb.readlistenapp.databinding.FragmentUsersBinding;
import com.williamnb.readlistenapp.features.chat.adapter.UsersAdapter;
import com.williamnb.readlistenapp.prefs.PreferenceManager;
import com.williamnb.readlistenapp.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends BaseFragment<FragmentUsersBinding, UsersViewModel> {

    private PreferenceManager preferenceManager;

    @Override
    public FragmentUsersBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentUsersBinding.inflate(inflater, container, false);
    }

    @Override
    public UsersViewModel createViewModel() {
        return new ViewModelProvider(this).get(UsersViewModel.class);
    }

    @Override
    public void initializeView() {
        hideBottomNavigationView(true);
        preferenceManager = new PreferenceManager(getContext());
    }

    @Override
    public void initializeComponent() {

    }

    @Override
    public void initializeEvents() {
        viewBinding.btnBack.setOnClickListener(view -> findNavController().popBackStack());
    }

    @Override
    public void initializeData() {
        getUsers();
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                            user.setEmail(queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                            user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setToken(queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users);
                            viewBinding.usersRcv.setAdapter(usersAdapter);
                            viewBinding.usersRcv.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        viewBinding.textErrorMessage.setText(String.format("%s", "Người dùng không tồn tại"));
        viewBinding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            viewBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            viewBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
