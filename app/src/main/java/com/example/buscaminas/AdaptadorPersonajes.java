package com.example.buscaminas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdaptadorPersonajes extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final String[] characterNames;
    private final int[] characterImages;

    public AdaptadorPersonajes(Context context, String[] characterNames, int[] characterImages) {
        super(context, 0, characterNames);
        this.inflater = LayoutInflater.from(context);
        this.characterNames = characterNames;
        this.characterImages = characterImages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_spinner_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.character_image);
            holder.textView = convertView.findViewById(R.id.character_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(characterImages[position]);
        holder.textView.setText(characterNames[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
