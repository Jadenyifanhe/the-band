import React from "react";
import Highcharts from "highcharts";
import HighchartsReact from "highcharts-react-official";
import { Track } from "../melody";

interface FrequencyPluginProps {
  data: Track[];
}

interface TitleArtist {
  title: string;
  artist: string;
}

interface DateData {
  frequency: number;
  items: TitleArtist[];
}

type ProcessedData = {
  [date: string]: DateData;
};

/**
 * Process the data to have the format expected by the frequency plugin
 * @param data track data
 * @returns frequnecy data of each date with the title and artist fall into that day
 */
const processData = (data: Track[]) => {
  console.log(data);
  const result = data.reduce((acc: ProcessedData, item: Track) => {
    // Extract the date from the timestamp
    const date = new Date(item.timestamp).toLocaleDateString();

    // If the date doesn't exist in the accumulator, initialize it
    if (!acc[date]) {
      acc[date] = {
        frequency: 0,
        items: [],
      };
    }

    // Increment the frequency and add the title-artist object to the list
    acc[date].frequency++;
    acc[date].items.push({ title: item.title, artist: item.artist });

    return acc;
  }, {});

  return result;
};

/**
 * Generates a bar chart showing the frequency of melody by date
 * @returns
 */
const FrequencyPlugin: React.FC<FrequencyPluginProps> = ({ data }) => {
  const freqData = processData(data);
  const sortedDates = Object.keys(freqData).sort((a, b) => {
    const dateA = new Date(a);
    const dateB = new Date(b);
    return dateA.getTime() - dateB.getTime();
  });

  const categories = sortedDates;
  const seriesData = sortedDates.map((date) => ({
    y: freqData[date].frequency,
    items: freqData[date].items,
  }));

  const chartOptions: Highcharts.Options = {
    chart: {
      type: "column",
      // height: "100%",
      // width: "100%",
    },
    title: {
      text: "Frequency of Melody by Date",
    },
    xAxis: {
      categories: categories,
      title: {
        text: "Date",
      },
    },
    yAxis: {
      min: 0,
      title: {
        text: "Frequency",
        align: "high",
      },
    },
    tooltip: {
      headerFormat: "<b>{point.x}</b><br/>",
      pointFormat: "Frequency: {point.y}<br/><br/>{point.items}",
      formatter: function () {
        const point = this.point as any;
        const itemsList = point.items
          .map((item: TitleArtist) => `${item.title} - ${item.artist}`)
          .join("<br/>");
        return `<b>${point.category}</b><br/>Frequency: ${point.y}<br/><br/>${itemsList}`;
      },
    },
    plotOptions: {
      bar: {
        dataLabels: {
          enabled: true,
        },
      },
    },
    series: [
      {
        name: "Frequency",
        data: seriesData,
        type: "column",
      },
    ],
  };

  return (
    <div>
      <HighchartsReact highcharts={Highcharts} options={chartOptions} />
    </div>
  );
};

export default FrequencyPlugin;
