package com.example.poisearch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.R;
import com.example.poisearch.model.RouteStep;

import java.util.List;

public class RouteStepAdapter extends RecyclerView.Adapter<RouteStepAdapter.StepViewHolder> {

    private List<RouteStep> stepList;

    public RouteStepAdapter(List<RouteStep> stepList) {
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        RouteStep step = stepList.get(position);
        holder.bind(step, position + 1);
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumber;
        TextView stepInstruction;
        TextView stepDistance;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumber = itemView.findViewById(R.id.step_number);
            stepInstruction = itemView.findViewById(R.id.step_instruction);
            stepDistance = itemView.findViewById(R.id.step_distance);
        }

        void bind(RouteStep step, int number) {
            stepNumber.setText(String.valueOf(number));
            stepInstruction.setText(step.getInstruction());

            if (step.getDistance() > 0) {
                if (step.getDistance() < 1000) {
                    stepDistance.setText(step.getDistance() + "米");
                } else {
                    stepDistance.setText(String.format("%.1f公里", step.getDistance() / 1000.0));
                }
                stepDistance.setVisibility(View.VISIBLE);
            } else {
                stepDistance.setVisibility(View.GONE);
            }
        }
    }
}
