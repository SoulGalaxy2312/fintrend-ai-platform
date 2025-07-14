from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from prophet import Prophet
from datetime import datetime
import pandas as pd

app = FastAPI()

class Candle(BaseModel):
    ds: datetime
    y: float
    sentiment_score: float

class PredictionResponse(BaseModel):
    predictedClose: float

@app.post("/predict", response_model=PredictionResponse)
def predict(candles: List[Candle]):
    df = pd.DataFrame([{
        "ds": c.ds.replace(tzinfo=None),
        "y": c.y,
        "sentiment_score": c.sentiment_score
    } for c in candles])

    model = Prophet()
    model.add_regressor("sentiment_score")
    model.fit(df)

    future = model.make_future_dataframe(periods=1, freq='min')
    future['sentiment_score'] = df['sentiment_score'].iloc[-1]  # broadcast score

    forecast = model.predict(future)
    print(forecast.tail())  # <-- thêm dòng này để debug

    predicted_close = forecast.iloc[-1]['yhat']
    print(f"Predicted close: {predicted_close}")
    return PredictionResponse(predictedClose=round(predicted_close, 2))