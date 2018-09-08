package com.phubber.ble.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.phubber.ble.R;

public class ContainerManager {
	private FragmentManager mFragmentManager;

	public ContainerManager(FragmentManager manager) {
		mFragmentManager = manager;
	}

	public void add(Fragment fragment) {
		add(R.id.frame_main, fragment, false);
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
	
	private void hide(int container, Fragment fragment)
	{
		mFragmentManager.beginTransaction().hide(fragment)
		.commitAllowingStateLoss();
	}
	private void show(int container, Fragment fragment)
	{
		mFragmentManager.beginTransaction().show(fragment)
		.commitAllowingStateLoss();
	}
}
