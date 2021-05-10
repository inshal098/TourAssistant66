package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourassistant.coderoids.R;
import com.visuality.f32.temperature.Temperature;
import com.visuality.f32.temperature.TemperatureUnit;
import com.visuality.f32.weather.data.entity.Weather;

import java.util.List;

public class ForecastTripWeatherAdapter extends RecyclerView.Adapter<ForecastTripWeatherAdapter.ViewHolder> {
    Context context;
    List<Weather> weathers;

    public ForecastTripWeatherAdapter(Context applicationContext, List<Weather> weathers) {
        this.context = applicationContext;
        this.weathers = weathers;
    }

    @NonNull
    @Override
    public ForecastTripWeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_weather_widget, viewGroup, false);
        return new ForecastTripWeatherAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ForecastTripWeatherAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            viewHolder.cityCurrent.setText(weathers.get(position).getNavigation().getLocationName());
            Temperature temperature = weathers.get(position).getTemperature().getCurrent();
            Double celcius = temperature.getValue(TemperatureUnit.CELCIUS);
            viewHolder.currentTemperatureField.setText(String.format("%.1f", celcius) + (char) 0x00B0+"C");
            if(weathers.get(position).getAtmosphere() != null)
                viewHolder.detailsField.setText("Humidity \n"+weathers.get(position).getAtmosphere().getHumidityPercentage()+"");

            if(weathers.get(position).getCloudiness().getPercentage() > 1 && weathers.get(position).getCloudiness().getPercentage() < 25){
                viewHolder.currentWeatherIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sunny));
            } else if( weathers.get(position).getCloudiness().getPercentage() >25 && weathers.get(position).getCloudiness().getPercentage() <50){
                viewHolder.currentWeatherIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sunny_clouds));
            } else if(weathers.get(position).getCloudiness().getPercentage() >50 && weathers.get(position).getCloudiness().getPercentage() <70){
                viewHolder.currentWeatherIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cloudy_day));
            } else if(weathers.get(position).getCloudiness().getPercentage() > 70){
                viewHolder.currentWeatherIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cloudy));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weathers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
       TextView cityCurrent,updatedCurrentField,currentTemperatureField,detailsField;
       ImageView currentWeatherIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cityCurrent =  itemView.findViewById(R.id.city_field);
            updatedCurrentField = itemView.findViewById(R.id.updated_field);
            currentWeatherIcon =  itemView.findViewById(R.id.weather_icon);
            currentTemperatureField =  itemView.findViewById(R.id.current_temperature_field);
            detailsField =  itemView.findViewById(R.id.details_field);
        }
    }
}



