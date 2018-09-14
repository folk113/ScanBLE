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

	public Fragment getFragmentByTag(String tag)
    {
        Fragment old = mFragmentManager.findFragmentByTag(tag);
        return old;
    }

	public void add(Fragment fragment,boolean addToBackStack) {
		add(R.id.frame_main, fragment, addToBackStack);
	}

	public void replace(Fragment oldFragment, Fragment newFragment) {
		replace(R.id.frame_main, oldFragment, newFragment);
	}

	private void add(int container, Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (addToBackStack)
		{
			transaction.replace(container, fragment);
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			transaction.addToBackStack(null);
		}
		else
		{
			boolean isAdded = false;
			for(Fragment frag:mFragmentManager.getFragments()) {
				if(frag.getClass().equals(fragment.getClass()))
				{
					isAdded = true;
					break;
				}
			}
			if(!isAdded)
				transaction.add(container, fragment);
		}
		transaction.commit();
	}

	public void remove(Fragment fragment)
	{
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.remove(fragment);
		transaction.commitAllowingStateLoss();
	}
	/**
	 * @param oldFragment
	 * @param newFragment
	 */
	private void replace(int container, Fragment oldFragment,
			Fragment newFragment) {
		mFragmentManager.beginTransaction()
				.replace(container, newFragment)
//				.addToBackStack(newFragment.getClass().getSimpleName())
				.commit();
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
	//context变了，重启后所有跟context相关联的资源都要重新获取
	public void release()
	{
		mInstance = null;
	}
}
