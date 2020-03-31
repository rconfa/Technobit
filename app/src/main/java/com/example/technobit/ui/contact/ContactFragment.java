package com.example.technobit.ui.contact;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.technobit.R;
import com.example.technobit.contactdatas.SingleContact;
import com.example.technobit.contactdatas.Singleton;

import java.util.ArrayList;

public class ContactFragment extends Fragment implements CardArrayAdapter.ItemLongClickListener{

    private ContactViewModel contactViewModel;
    private CardArrayAdapter cardArrayAdapter;
    private RecyclerView recView;
    private Singleton sg;
    private ArrayList<Integer> posToBeRemoved;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);

        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contact, container, false);

        // save the singleton instance
        sg = Singleton.getInstance(getContext());
        // list of position to removed
        posToBeRemoved = new ArrayList<>();

        recView = root.findViewById(R.id.contact_listview);
        recView.setHasFixedSize(true); // no change the layout size
        // setting layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recView.setLayoutManager(layoutManager);

        // adapter for the listview
        cardArrayAdapter = new CardArrayAdapter();
        // add all item to the adapter
        addContactToAdapter();

        // set the long click listener equal to my local listener
        cardArrayAdapter.mySetLongClickListener(this);

        // set the adapter for the listview
        recView.setAdapter(cardArrayAdapter);

        /* TODO: edit existing contact??*/

        return root;
    }

    private void addContactToAdapter() {
        // retrive all contact from file
        ArrayList<SingleContact> allContact = sg.getContactList();
        if(allContact != null)
            cardArrayAdapter.add(allContact); // add all list to adapter
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // carico il file per il menu, cosi visualizzo le icone per aggiungere/eliminare un contatto
        inflater.inflate(R.menu.menu_contact, menu); //.xml file name
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Handling Action Bar button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.icon_remove:
                // remove all items selected from file
                sg.delete(posToBeRemoved,getContext());

                // clear the list of items to be removed
                posToBeRemoved.clear();
                // remove all items from the listview
                cardArrayAdapter.removeSelected();

                return true;
            case R.id.icon_add:
                displayAddContactDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // creo la view sfruttando il mio custom layout
        View v = getLayoutInflater().inflate(R.layout.custom_dialog_add_contact, null);

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        // Accesso agli oggetti della view
        final Button yes = v.findViewById(R.id.btn_yes);
        final Button no = v.findViewById(R.id.btn_no);
        final EditText et_name = v.findViewById(R.id.et_dialog_name);
        final EditText et_email = v.findViewById(R.id.et_dialog_email);

        // voglio che il nome sia obbligatorio, se è vuoto non abilito il bottone "ok"
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0) // se ha scritto il nome attivo il bottone ok
                    yes.setVisibility(View.VISIBLE);
                else
                    yes.setVisibility(View.INVISIBLE);
            }

        });

        // se clicca ok aggiungo al mio adapter i nuovi valori e chiudo la finestra!
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prendo i valori
                String name = et_name.getText().toString();
                String email = et_email.getText().toString();
                // aggiungo i valori
                SingleContact contact = new SingleContact(name, email);
                sg.addContact(contact, getContext());
                cardArrayAdapter.add(new Card(contact));

                // chiudo la dialog
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // visualizzo la dialog
        dialog.show();
    }


    @Override
    public void onItemLongClick(View view, int position) {
        // salvo la posizone selezionata
        boolean res = cardArrayAdapter.savePositionToDelete(position);
        if(res) { // se ho aggiunto la posizione metto come colore di sfondo il rosso
            view.setBackgroundResource(R.drawable.card_background_selected);
            posToBeRemoved.add(position);
        }
        else { // Se l'ho deselezionato rimetto lo sfondo bianco
            view.setBackgroundResource(R.drawable.card_background);
            posToBeRemoved.remove((Object) position); // I want to remove the obj not the index
        }
    }
}