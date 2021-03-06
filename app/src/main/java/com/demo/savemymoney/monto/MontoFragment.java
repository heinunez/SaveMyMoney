package com.demo.savemymoney.monto;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.demo.savemymoney.R;
import com.demo.savemymoney.common.BaseFragment;
import com.demo.savemymoney.common.dto.ErrorMessage;
import com.demo.savemymoney.common.notification.Notifier;
import com.demo.savemymoney.data.entity.Income;
import com.demo.savemymoney.databinding.MontoFragmentBinding;
import com.demo.savemymoney.main.MainActivity;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.kolotnev.formattedittext.CurrencyEditText;

import static com.demo.savemymoney.common.util.DateUtils.getMillisUntil;
import static com.demo.savemymoney.common.util.DateUtils.isSameDay;


public class MontoFragment extends BaseFragment implements MontoFragmentPresenter.View {

    @BindView(R.id.monto_textInputLayout)
    TextInputLayout montoTil;
    @BindView(R.id.txt_Monto)
    CurrencyEditText txtMonto;
    @BindView(R.id.txt_Fecha)
    EditText txtFechaInicio;

    @BindView(R.id.monto_mensual_radio)
    RadioButton mensualRadio;

    @BindView(R.id.monto_quincenal_radio)
    RadioButton QuincenalRadio;

    @BindView(R.id.monto_frequency_radiogroup)
    RadioGroup frequencyGroup;


    private DatePickerDialog.OnDateSetListener mDateSetListener;

    MontoFragmentPresenter presenter;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private Income incomeBeforeChanges;

    public static MontoFragment newInstance() {
        return new MontoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MontoFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.monto_fragment, container, false);
        presenter = new MontoFragmentPresenter(this, getContext());
        binding.setPresenter(presenter);

        View view = binding.getRoot();

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
        presenter.loadIncome();
        getActivity().setTitle(R.string.income_update_title);
    }

    private void initUI() {

        frequencyGroup.check(R.id.monto_mensual_radio);

        txtFechaInicio.setOnClickListener(view12 -> {

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            int year = cal.get(cal.YEAR);
            int month = cal.get(cal.MONTH);
            int day = cal.get(cal.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(), mDateSetListener
                    , year, month, day);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String fecha = day + "/" + month + "/" + year;


            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            int anio = cal.get(cal.YEAR);
            int mes = cal.get(cal.MONTH);
            int dia = cal.get(cal.DAY_OF_MONTH);
            mes = mes+1;

            String fechahoy = dia + "/" + mes + "/" + anio;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date hoy = new Date();

            Date fechaseleccionada =null;
            try {
                fechaseleccionada = sdf.parse(fecha);

            } catch (ParseException e) {
                e.printStackTrace();
            }



            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
                // versiones con android 6.0 o superior
                txtFechaInicio.setText(fecha);
            } else{
                // para versiones anteriores a android 6.0
                if(fechaseleccionada.after(hoy)|| fecha.equals(fechahoy)){
                    txtFechaInicio.setText(fecha);
                } else {
                    Toast.makeText(getContext(),"Seleccione fecha de hoy o posterior",Toast.LENGTH_LONG).show();

                }
            }







        };

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int calendarTime = Calendar.DAY_OF_MONTH;
        int temp = calendar.get(calendarTime);
        calendar.set(calendarTime, temp);

        Date fecha = calendar.getTime();
        String fecha_Actual = dateFormat.format(fecha);
        txtFechaInicio.setText(fecha_Actual);
    }


    @Override
    public void showErrorMessages(List<ErrorMessage> errors) {

        for (ErrorMessage error : errors) {
            if (error.getInputId() == null)
                showError(error.getMessage());
            else {
                TextInputLayout input = getActivity().findViewById(error.getInputId());
                input.setErrorEnabled(true);
                input.setError(error.getMessage());
            }
        }
    }

    @Override
    public void clearErrorMessages() {
        montoTil.setErrorEnabled(false);
        montoTil.setError("");
    }

    @Override
    public void showProgress() {
        showProgressDialog(R.string.income_loading);
    }

    @Override
    public void hideProgress() {
        hideProgressDialog();
    }

    @Override
    public Income getIncome() {
        BigDecimal monto = txtMonto.getValue();
        String usuario = mAuth.getCurrentUser().getUid();
        String selecion = frequencyGroup.getCheckedRadioButtonId() == mensualRadio.getId() ? "MENSUAL" : "QUINCENAL";
        String fechaInicio = txtFechaInicio.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaInicio_Date = null;
        try {
            fechaInicio_Date = sdf.parse(fechaInicio);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Income income = new Income();
        income.userUID = usuario;
        income.amount = monto.doubleValue();
        income.period = selecion;
        income.startDate = fechaInicio_Date;
        if (incomeBeforeChanges != null)
            income.payDate = incomeBeforeChanges.payDate;

        return income;
    }

    @Override
    public void notifyIncomeSaved() {
        SweetAlertDialog alert = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.success_title));
        if (getActivity().getClass().equals(MontoActivity.class)) {
            alert.setContentText(getString(R.string.income_succes_save))
                    .setConfirmText(getString(R.string.income_confirm_save))
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        getActivity().finish();
                    });
        } else {
            alert.setContentText(getString(R.string.income_succes_save_main))
                    .setConfirmClickListener(sDialog -> getActivity().onBackPressed());
        }
        alert.setCancelable(false);
        alert.show();
    }

    @Override
    public void loadValues(Income result) {
        incomeBeforeChanges = result;
        txtMonto.setValue(BigDecimal.valueOf(result.getAmount()));
        txtFechaInicio.setText(dateFormat.format(result.startDate));
        if ("MENSUAL".equals(result.period))
            frequencyGroup.check(R.id.monto_mensual_radio);
        else
            frequencyGroup.check(R.id.monto_quincenal_radio);

    }

    @Override
    public void prepareFutureNotification(Income income) {
        if (isSameDay(income.startDate, new Date()))
            presenter.increaseMainAmount();
        else
            Notifier.scheduleMainAmount(getContext(),
                    getMillisUntil(income.startDate),
                    mAuth.getCurrentUser().getUid());

    }

}
