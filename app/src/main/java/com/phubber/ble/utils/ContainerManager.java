package com.phubber.ble.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.phubber.ble.R;

public class ContainerManager {
	private FragmentManager mFragmentManager;

	private static ContainerManager mInstance;

	public static ContainerManager getInstance()
	{
		return mInstance;
	}

	public static ContainerManager getInstance(FragmentManager manager)
	{
		if(mInstance == null)
		{
			mInstance = new ContainerManager(manager);
		}
		return mInstance;
	}

	private ContainerManager(FragmentManager manager) {
		mFragmentManager = manager;
	}

	public void add(Fragment fragment,boolean addToBackStack) {
		add(R.id.frame_main, fragment, addToBackStack);
	}

	public void replace(Fragment oldFragment, Fragment newFragment) {
		replace(R.id.frame_main, oldFragment, newFragment);
	}

	private void add(int container, Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.add(container, fragment);
		if (addToBackStack)
			transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commitAllowingStateLoss();
	}

	/**
	 * @param oldFragment
	 * @param newFragment
	 */
	private void replace(int container, Fragment oldFragment,
			Fragment newFragment) {
		mFragmentManager.beginTransaction().remove(oldFragment)
				.add(container, newFragment)
				.addToBackStack(newFragment.getClass().getSimpleName())
				.commitAllowingStateLoss();
	}
	
	public void hide(Fragment fragment)
	{
		mFragmentManager.beginTransaction().hide(fragment)
		.commitAllowingStateLoss();
	}
	public void show(Fragment fragment)
	{
		mFragmentManager.beginTransaction().show(fragment)
		.commitAllowingStateLoss();
	}
}
