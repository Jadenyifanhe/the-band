import * as React from "react";
import Highcharts from "highcharts";
import HighchartsReact from "highcharts-react-official";
import { Track } from "../melody";

interface GenreCount {
  genre: string;
  value: number;
  titles: string[];
}

interface GenrePluginProps {
  data: Track[];
}

/**
 * Process the data to be the format expected by the genre plugin
 * @param data track data
 * @returns genre with its count and titles fall into the category
 */
const countGenreAppearances = (data: Track[]): GenreCount[] => {
  const result: GenreCount[] = [];

  data.forEach((item: Track) => {
    item.genre.forEach((genre: string) => {
      const index = result.findIndex((entry) => entry.genre === genre);

      if (index === -1) {
        // Genre not found in result array, add it with an initial value of 1
        result.push({ genre, value: 1, titles: [item.title] });
      } else {
        // Genre found, increment the value
        result[index].value++;
        // add the title to the array
        result[index].titles.push(item.title);
      }
    });
  });

  return result;
};

/**
 * Generates the pie chart showing the distribution of the genres of the melody
 * @returns
 */
const GenrePlugin: React.FC<GenrePluginProps> = ({ data }) => {
  const genreCounts = countGenreAppearances(data);

  const chartOptions: Highcharts.Options = {
    chart: {
      type: "pie",
    },
    title: {
      text: "Genre Counts",
    },
    tooltip: {
      headerFormat: "<b>{point.key}</b><br/>",
      pointFormat:
        "{series.name}: <b>{point.percentage:.1f}%</b><br/>Tracks: {point.titles}",
      formatter: function () {
        const point = this.point as any;
        return `<b>${point.name}</b><br/>${
          point.series.name
        }: <b>${point.percentage.toFixed(
          1
        )}%</b><br/>Tracks: ${point.titles.join(",")}`;
      },
    },
    plotOptions: {
      pie: {
        allowPointSelect: true,
        cursor: "pointer",
        dataLabels: {
          enabled: true,
          format: "<b>{point.name}</b>: {point.percentage:.1f} %",
        },
      },
    },
    series: [
      {
        name: "Percentage",
        data: genreCounts.map((item) => ({
          name: item.genre,
          y: item.value,
          titles: item.titles,
        })),
        type: "pie",
      },
    ],
  };
  return (
    <div>
      <HighchartsReact highcharts={Highcharts} options={chartOptions} />
    </div>
  );
};

export default GenrePlugin;
