import React from "react";
import Highcharts from "highcharts";
import HighchartsReact from "highcharts-react-official";
import { Track } from "../melody";

interface SentimentData {
  x: number;
  y: number;
  title: string;
}

interface SentimentPluginProps {
  data: Track[];
}

/**
 * Process the data to be the format expected by the sentiment plugin
 * @param data track data
 * @returns processed data to have x, y and title properties
 */
const processData = (data: Track[]): SentimentData[] => {
  return data.map((item: Track) => ({
    x: parseFloat(item.score[1].toFixed(3)),
    y: parseFloat(item.score[0].toFixed(3)),
    title: item.title,
  }));
};

/**
 * Generates the sentiment scatter plot showing the valence and arousal values
 * @returns
 */
const SentimentPlugin: React.FC<SentimentPluginProps> = ({ data }) => {
  const sentimentData = processData(data);

  const chartOptions: Highcharts.Options = {
    chart: {
      type: "scatter",
      height: 600,
      width: 600,
      backgroundColor: "rgba(0,0,0,0)",
    },
    title: {
      text: "Sentiment Analysis",
    },
    xAxis: {
      title: {
        text: "Valence",
      },
      min: -1,
      max: 1,
      gridLineWidth: 1,
    },
    yAxis: {
      title: {
        text: "Arousal",
      },
      min: -8,
      max: 8,
      gridLineWidth: 1,
    },
    plotOptions: {
      scatter: {
        marker: {
          radius: 5,
        },
      },
    },
    tooltip: {
      headerFormat: "<b>{point.key}</b><br/>",
      pointFormat: "X: {point.x}, Y: {point.y}",
      formatter: function () {
        const point = this.point as any;
        return `<b>${point.title}</b><br/>X: ${point.x}, Y: ${point.y}`;
      },
    },
    series: [
      {
        name: "Data Points",
        data: sentimentData.map((point) => ({
          x: point.x,
          y: point.y,
          title: point.title,
        })),
        type: "scatter",
      },
    ],
  };

  return (
    <div
      style={{
        backgroundImage: "url(/sentiment.png)",
        backgroundSize: "80%",
        backgroundPosition: "85px 35px",
        backgroundRepeat: "no-repeat",
      }}
    >
      <HighchartsReact highcharts={Highcharts} options={chartOptions} />
    </div>
  );
};

export default SentimentPlugin;
