package com.everyware.handheld;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.AttributeInfo;
import org.ksoap2.serialization.SoapObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.everyware.handheld.adapter.CategoryAdapter;
import com.everyware.handheld.adapter.FoodCategoryItemAdapter;
import com.everyware.handheld.adapter.OrderListAdapter;
import com.everyware.handheld.bean.CategoryDto;
import com.everyware.handheld.bean.ItemMasterDto;
import com.everyware.handheld.bean.OrderListBean;
import com.everyware.handheld.bean.ReasonDto;
import com.everyware.handheld.bean.TxSalesDetailDto;
import com.everyware.handheld.http.AsyncHttpRequest;
import com.everyware.handheld.http.HandlerCallBack;
import com.everyware.handheld.http.HttpHandler;
import com.everyware.handheld.utils.CommonUtils;
import com.everyware.handheld.utils.ComponentsManager;
import com.everyware.handheld.utils.ConstantUtils;
import com.everyware.handheld.utils.FormatCommitSoapObject;
import com.everyware.handheld.utils.GetCategoryDto;
import com.everyware.handheld.utils.GetItemMasterDto;
import com.everyware.handheld.utils.GetReasonDto;
import com.everyware.handheld.utils.HandleHttpRequestResult;
import com.everyware.handheld.utils.HandlerOrderListCallBack;
import com.everyware.handheld.utils.ItemMasterDtoToTxSalesDetailDto;
import com.everyware.handheld.utils.OrderListUtils;
import com.everyware.handheld.utils.SoapUtils;
import com.everyware.handheld.view.Configure;
import com.everyware.handheld.view.ScrollLayout;

/**
 * 
 * @author ALEX
 * 
 */
public class OrderListExpansion extends Activity {
	
	private LinearLayout calculateLayout = null;
	private Button btnkeyPan = null;
	private Button btnSendOrder = null;
	private Button btncollapse=null;
	private boolean isShow = true;
	private LinearLayout linearlayoutKeyPan = null;

	private String previouslevel=null;
	private String cateid=null;
	private String smartcategory=null;
	
	private ListView listView = null;
	private OrderListAdapter mOrderListAdapter = null;

	private int seq = 0;

	
	private Intent intent = null;
    private Context currentPageContext = OrderListExpansion.this;

    private OrderListBean selectedOrderListBean;
    private OrderListBean selectedParentOrderListBean;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_expand);
		ComponentsManager.getComponentManager().pushComponent(this);

		initOrderList();
		initViews();
	}


	private void initViews() {
		calculateLayout = (LinearLayout) findViewById(R.id.calculateLayout);
		btnkeyPan = (Button) findViewById(R.id.btnkeyPan);
		btnSendOrder = (Button) findViewById(R.id.btnSendOrder);
		linearlayoutKeyPan = (LinearLayout) findViewById(R.id.linearlayoutKeyPan);
		btncollapse=(Button) findViewById(R.id.btn_coll);
		btnkeyPan.setOnClickListener(listener);
		btnSendOrder.setOnClickListener(listener);
        linearlayoutKeyPan.setOnClickListener(listener);
        btncollapse.setOnClickListener(listener);
        
        intent = getIntent();
		if (null != intent) {
        previouslevel=intent.getStringExtra("UpperLevelPanel");
        if(previouslevel.equalsIgnoreCase("FoodCatgoryItemActivity"))
        {
        	cateid=intent.getStringExtra("categoryid");
        	smartcategory=intent.getStringExtra("smartcategory");	
        
        }
        
		}
	}

	/**
	 * 鍒濆鍖栬鍗�
	 */
	private void initOrderList() {
        try {
            listView = (ListView) findViewById(R.id.listView);
            mOrderListAdapter = new OrderListAdapter(this, ConstantUtils.orderList,
                    new HandlerOrderListCallBack() {

                        @Override
                        public void handlerCallBack(int parentPosition,
                                int position, int subPosition, String status) {
                            //changeOrderListItem(parentPosition, position,
                                   // subPosition, status);
                        }
                    });
            listView.setAdapter(mOrderListAdapter);
        } catch (Exception e) {
            CommonUtils.showToast(OrderListExpansion.this,
                    getString(R.string.remote_server_error));
        }
    }

	

	/**
	 * 鐐瑰嚮浜嬩欢
	 */
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
            try {
                switch (v.getId()) {

                case R.id.btn_coll:
                    backtomain();
                    //commitOrderListAndPrintBill();
                    break;
                case R.id.btnSendOrder:
                    commitOrderList();
                    break;
                case R.id.linearlayoutKeyPan:
                	showerrorlog();
                	break;
                }
            } catch (Exception e) {
                CommonUtils.showToast(OrderListExpansion.this,
                        getString(R.string.remote_server_error));
            }
        }
	};
	
	private void showerrorlog()
	{
		 CommonUtils.showToast(OrderListExpansion.this,
				 "This button is not allowed when order list being expanded");
		  
      
		
	}
	private void backtomain()
	{
		
		if(previouslevel.equalsIgnoreCase("FoodCategoryActivity"))
		{
		Intent intent = new Intent(OrderListExpansion.this,
				FoodCategoryActivity.class);
		intent.putExtra("loading", "second");
		startActivity(intent);
		}
		else if(previouslevel.equalsIgnoreCase("FoodCatgoryItemActivity"))
		{
			
			
			
			Intent intent = new Intent(OrderListExpansion.this,
					FoodCatgoryItemActivity.class);
			intent.putExtra("categoryId", cateid);
			intent.putExtra("isSmartCategory", smartcategory);
			startActivity(intent);
		}
	}
	
	 private List<TxSalesDetailDto> reassignSeqIndxToOrderList(){
	        if (null == ConstantUtils.orderList
	                || ConstantUtils.orderList.size() <= 0) {
	            return null;
	        }
	        List<TxSalesDetailDto> dtoList = new ArrayList<TxSalesDetailDto>();
	        OrderListBean bean = null;
	        List<OrderListBean> modifierList = null;
	        OrderListBean modifierListBean = null;
	        List<OrderListBean> followSetList = null;
	        OrderListBean followSetListBean = null;
	        List<OrderListBean> followSetModifierList = null;
	        OrderListBean followSetModifierListBean = null;
	        List<OrderListBean> modifier = null;
	        OrderListBean modifierBean = null;
	        List<OrderListBean> followSet = null;
	        OrderListBean followSetBean = null;
	        List<OrderListBean> followSetModifier = null;
	        OrderListBean followSetModifierBean = null;
	        try {
	            int seqNo = ConstantUtils.maxSeqNumber;
	            int itemOrderRunningIndex = ConstantUtils.maxItemOrderRunningIndex;
	            int itemSetRunningIndex = ConstantUtils.maxItemSetRunningIndex;
	            for (int i = 0; i < ConstantUtils.orderList.size(); i++) {
	                bean = ConstantUtils.orderList.get(i);
	                if (null != bean) {
	                    if (null != bean.getDetailDto()) {
	                        dtoList.add(bean.getDetailDto());
	                        modifierList = bean.getModifierDto();
	                        if (null != modifierList && modifierList.size() > 0) {
	                            for (int j = 0; j < modifierList.size(); j++) {
	                                modifierListBean = modifierList.get(j);
	                                if (null != modifierListBean) {
	                                    if (null != modifierListBean.getDetailDto()) {
	                                        dtoList.add(modifierListBean.getDetailDto());
	                                    }
	                                    modifierListBean = null;
	                                }
	                            }
	                            modifierList = null;
	                        }
	                        followSetList = bean.getFollowSetDto();
	                        if (null != followSetList && followSetList.size() > 0) {
	                            for (int j = 0; j < followSetList.size(); j++) {
	                                followSetListBean = followSetList.get(j);
	                                if (null != followSetListBean) {
	                                    if (null != followSetListBean.getDetailDto()) {
	                                        dtoList.add(followSetListBean
	                                                .getDetailDto());
	                                    }
	                                    followSetModifierList = followSetListBean
	                                            .getModifierDto();
	                                    if (null != followSetModifierList
	                                            && followSetModifierList.size() > 0) {
	                                        for (int k = 0; k < followSetModifierList
	                                                .size(); k++) {
	                                            followSetModifierListBean = followSetModifierList
	                                                    .get(k);
	                                            if (null != followSetModifierListBean) {
	                                                if (null != followSetModifierListBean
	                                                        .getDetailDto()) {
	                                                    dtoList.add(followSetModifierListBean
	                                                            .getDetailDto());
	                                                }
	                                                followSetModifierListBean = null;
	                                            }
	                                        }
	                                        followSetModifierList = null;
	                                    }
	                                    followSetListBean = null;
	                                }
	                            }
	                            followSetList = null;
	                        }
	                    } else {
	                        if (null != bean.getDto()) {
	                            seqNo++;
	                            itemOrderRunningIndex++;
	                            itemSetRunningIndex++;
	                            dtoList.add(ItemMasterDtoToTxSalesDetailDto
	                                    .getTxSalesDetailDto(bean.getDto(),
	                                            bean.isItemOnHold(), seqNo,
	                                            itemOrderRunningIndex,
	                                            itemSetRunningIndex, "0"));
	                            modifier = bean.getModifier();
	                            if (null != modifier && modifier.size() > 0) {
	                                for (int j = 0; j < modifier.size(); j++) {
	                                    modifierBean = modifier.get(j);
	                                    if (null != modifierBean) {
	                                        if (null != modifierBean.getDto()) {
	                                            seqNo++;
	                                            dtoList.add(ItemMasterDtoToTxSalesDetailDto.getTxSalesDetailDto(
	                                                    modifierBean.getDto(),
	                                                    modifierBean.isItemOnHold(),
	                                                    seqNo, itemOrderRunningIndex,
	                                                    itemSetRunningIndex, "1"));
	                                        }
	                                        modifierBean = null;
	                                    }
	                                }
	                                modifier = null;
	                            }
	                            followSet = bean.getFollowSet();
	                            if (null != followSet && followSet.size() > 0) {
	                                for (int j = 0; j < followSet.size(); j++) {
	                                    followSetBean = followSet.get(j);
	                                    if (null != followSetBean) {
	                                        if (null != followSetBean.getDto()) {
	                                            seqNo++;
	                                            itemSetRunningIndex++;
	                                            dtoList.add(ItemMasterDtoToTxSalesDetailDto.getTxSalesDetailDto(
	                                                    followSetBean.getDto(),
	                                                    followSetBean.isItemOnHold(),
	                                                    seqNo, itemOrderRunningIndex,
	                                                    itemSetRunningIndex, "2"));
	                                            followSetModifier = followSetBean
	                                                    .getModifier();
	                                            if (null != followSetModifier
	                                                    && followSetModifier.size() > 0) {
	                                                for (int k = 0; k < followSetModifier
	                                                        .size(); k++) {
	                                                    followSetModifierBean = followSetModifier
	                                                            .get(k);
	                                                    if (null != followSetModifierBean) {
	                                                        if (null != followSetModifierBean
	                                                                .getDto()) {
	                                                            seqNo++;
	                                                            dtoList.add(ItemMasterDtoToTxSalesDetailDto
	                                                                    .getTxSalesDetailDto(
	                                                                            followSetModifierBean
	                                                                                    .getDto(),
	                                                                            followSetModifierBean
	                                                                                    .isItemOnHold(),
	                                                                            seqNo,
	                                                                            itemOrderRunningIndex,
	                                                                            itemSetRunningIndex,
	                                                                            "3"));
	                                                        }
	                                                        followSetModifierBean = null;
	                                                    }
	                                                }
	                                                followSetModifier = null;
	                                            }
	                                        }
	                                        followSetBean = null;
	                                    }
	                                }
	                                followSet = null;
	                            }
	                        }
	                    }
	                    bean = null;
	                }
	            }
	        } catch (Exception e) {
	            CommonUtils.showToast(OrderListExpansion.this,
	                    getString(R.string.remote_server_error));
	        }

	        return dtoList;
	    }
	 private void commitOrderList() {

	        try {
	            List<TxSalesDetailDto> dtoList = reassignSeqIndxToOrderList();

	            if (null == dtoList || dtoList.size() <= 0) {
	                return;
	            } else {
	                commitData(dtoList, true, false);
	            }
	        } catch (Exception e) {
	            CommonUtils.showToast(OrderListExpansion.this,
	                    getString(R.string.remote_server_error));
	        }
	    }
	 private void setTableStatus(int tableStatusId) {
	        try {
	            SoapObject mSoapObject = new SoapObject(SoapUtils.TARGET_NAMESPACE,
	                    SoapUtils.SET_TABLE_STATUS_BY_TABLE_ID);

	            AttributeInfo tem = new AttributeInfo();
	            tem.setName("xmlns:tem");
	            tem.setValue(SoapUtils.TARGET_NAMESPACE);
	            mSoapObject.addAttribute(tem);

	            AttributeInfo pos = new AttributeInfo();
	            pos.setName("xmlns:pos");
	            pos.setValue(SoapUtils.POS_NAMESPACE);
	            mSoapObject.addAttribute(pos);

	            mSoapObject.addProperty("tem:" + ConstantUtils.TABLE_ID,
	                    ConstantUtils.mTableDto.getTableId());
	            mSoapObject.addProperty("tem:" + ConstantUtils.TABLE_STATUS_ID, tableStatusId);
	            mSoapObject.addProperty("tem:" + ConstantUtils.POS_CODE, CommonUtils.getSharedPreferences(this,
	                    this.getString(R.string.device_name)));
	            mSoapObject.addSoapObject(FormatCommitSoapObject.getFormatUserInfo());


	            new AsyncHttpRequest(this, isShow, mSoapObject,
	                    SoapUtils.SET_TABLE_STATUS_BY_TABLE_ID,
	                    new HandlerCallBack() {

	                        @Override
	                        public void handleFinish(HashMap<String, Object> result) {
	                            try {
	                                if (HandleHttpRequestResult.isError(
	                                        OrderListExpansion.this, isShow,
	                                        SoapUtils.SET_TABLE_STATUS_BY_TABLE_ID,
	                                        result)) {
	                                    return;
	                                }

	                                SoapObject object = (SoapObject) result
	                                        .get(HttpHandler.RESULT);

	                            } catch (Exception e) {
	                                CommonUtils.showToast(OrderListExpansion.this,
	                                        getString(R.string.remote_server_error));
	                            }
	                        }

	                        @Override
	                        public void handleFailure() {
	                            CommonUtils.showToast(OrderListExpansion.this,
	                                    getString(R.string.remote_server_error));
	                        }
	                    }).execute();
	        } catch (Exception e) {
	            CommonUtils.showToast(OrderListExpansion.this,
	                    getString(R.string.remote_server_error));
	        }
	    }
	 private void commitData(List<TxSalesDetailDto> dtoList, final boolean isShow, final boolean isPrintBill) {
	        SoapObject mSoapObject = new SoapObject(SoapUtils.TARGET_NAMESPACE,
	                SoapUtils.SAVE_TX_SALES);
	        String isInvoicePrintPending = "false";

	        try {
	            AttributeInfo tem = new AttributeInfo();
	            tem.setName("xmlns:tem");
	            tem.setValue(SoapUtils.TARGET_NAMESPACE);
	            mSoapObject.addAttribute(tem);

	            AttributeInfo pos = new AttributeInfo();
	            pos.setName("xmlns:pos");
	            pos.setValue(SoapUtils.POS_NAMESPACE);
	            mSoapObject.addAttribute(pos);

	            if(isPrintBill) {
	                ConstantUtils.mTxSalesHearderDto.setTxChecked("true");
	            }

	            mSoapObject.addSoapObject(FormatCommitSoapObject
	                    .getFormatTxSalesHeaderDto(ConstantUtils.mTxSalesHearderDto,
	                            ConstantUtils.mTxPaymentDto,
	                            ConstantUtils.mTxSalesDetailDtoNormal));
	            mSoapObject.addSoapObject(FormatCommitSoapObject
	                    .getFormatTxSalesDetailDtoList(dtoList, true));

	            mSoapObject.addSoapObject(FormatCommitSoapObject.getFormatUserInfo());

	            // check if server need to reprint the checklist
	            if(isPrintBill) {
	                isInvoicePrintPending = "true";
	            } else {
	                for(int i = 0; i <dtoList.size(); i++ ) {
	                    if("true".equals(dtoList.get(i).getIsLocalChangedItem())){
	                        if("0".equals(dtoList.get(i).getTxSalesDetailId())) {
	                            isInvoicePrintPending = "true";
	                            break;
	                        } else if("false".equals(dtoList.get(i).getEnabled())) {
	                            isInvoicePrintPending = "true";
	                            break;
	                        }
	                    }
	                }
	            }

	            mSoapObject.addProperty("tem:" + ConstantUtils.IS_INVOICE_PRINT_PENDING, isInvoicePrintPending);


	            new AsyncHttpRequest(this, isShow, mSoapObject,
	                    SoapUtils.SAVE_TX_SALES, new HandlerCallBack() {

	                @Override
	                public void handleFinish(HashMap<String, Object> result) {
	                    if (HandleHttpRequestResult.isError(
	                           OrderListExpansion.this, isShow,
	                            SoapUtils.SAVE_TX_SALES, result)) {
	                        return;
	                    }
	                    CommonUtils.showToast(OrderListExpansion.this,
	                            getString(R.string.commit_order_success));
	                    //ConstantUtils.isSubmit = true;

	                    //finish();
	                    if(isPrintBill) {
	                        setTableStatus(ConstantUtils.TABLE_STATUS_ORDER_CHECK);
	                    } else {
	                        setTableStatus(ConstantUtils.TABLE_STATUS_ORDER_COMPLETE);
	                    }

	                    ConstantUtils.isSplit = false;
	                    CommonUtils.changeActivity(OrderListExpansion.this,
	                            TableListFragmentActivity.class);
	                }

	                @Override
	                public void handleFailure() {

	                }
	            }).execute();
	        } catch (Exception e) {
	            CommonUtils.showToast(OrderListExpansion.this,
	                    getString(R.string.remote_server_error));
	        }
	    }
 
	
	
}

