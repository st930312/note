### android fragment 筆記:

## 小問題：
* supportFragmentManager在replace fragment的時候，如果遇到相同的實體，則不會進行lifecycle
* 使用 requireActivity() 替代 getActivity()
* 檢查view是否正確出現在螢幕上:
  ```java

                                Rect rect = new Rect();
                                boolean getGlobalVisibleRect = view.getGlobalVisibleRect(rect);
                                Log.d(TAG, "OnPreDrawListener.getGlobalVisibleRect = " + getGlobalVisibleRect);

                                if (!getGlobalVisibleRect) {

                                    view.measure(mainActivityBinding.getRoot().getMeasuredWidth(), mainActivityBinding.getRoot().getMeasuredHeight());
                                    view.layout(mainActivityBinding.getRoot().getLeft(),
                                            mainActivityBinding.getRoot().getTop(),
                                            mainActivityBinding.getRoot().getRight(),
                                            mainActivityBinding.getRoot().getBottom());

                                    view.forceLayout();
                                    view.invalidate();
                                }
  ```