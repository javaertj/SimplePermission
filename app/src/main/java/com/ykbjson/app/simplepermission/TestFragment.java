package com.ykbjson.app.simplepermission;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ykbjson.lib.simplepermission.ano.PermissionNotify;
import com.ykbjson.lib.simplepermission.ano.PermissionRequest;

/**
 * Description：
 * Creator：yankebin
 * CreatedAt：2018/11/8
 */
@PermissionNotify
public class TestFragment extends Fragment {
    private TextView mTextMessage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        mTextMessage = view.findViewById(R.id.message);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextMessage.setText(getArguments().getString("text"));
        test();
    }

    @PermissionRequest(
            requestCode = 100,
            requestPermissions = {Manifest.permission.READ_SMS},
            needReCall = true)
    private void test() {
        Toast.makeText(getActivity(), "呵呵呵呵呵", Toast.LENGTH_LONG).show();
    }
}
