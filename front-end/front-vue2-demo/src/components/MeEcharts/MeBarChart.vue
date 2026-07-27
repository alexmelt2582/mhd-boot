<template>
  <div :id="id" :style="{ height: height, width: width }" />
</template>

<script>
export default {
  name: "MeBarChart",
  props: {
    id: {
      type: String,
      required: true
    },
    width: {
      type: String,
      default: "100%"
    },
    height: {
      type: String,
      default: "300px"
    },
    chartData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      chart: null,
      modalData: []
    };
  },
  watch: {
    chartData: {
      handler() {
        this.$nextTick(() => {
          this.initChart();
        });
      },
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart();
    });
  },
  beforeDestroy() {
    if (!this.chart) {
      return;
    }
    this.chart.dispose();
    this.chart = null;
  },
  methods: {
    initChart() {
      // 检查 DOM 元素是否存在
      const chartDom = document.getElementById(this.id);
      if (!chartDom) {
        console.warn(`Chart container with id "${this.id}" not found`);
        return;
      }
      // 如果已存在图表实例，先销毁
      if (this.chart) {
        this.chart.dispose();
        this.chart = null;
      }
      // 检查数据是否为空
      if (!this.chartData || this.chartData.length === 0) {
        console.warn("Chart data is empty");
        return;
      }
      const names = this.chartData.map(item => item.name);
      const values = this.chartData.map(item => item.value);
      this.chart = this.$echarts.init(chartDom);
      this.chart.setOption({
        xAxis: {
          type: 'category',
          data: names
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            data: values,
            type: 'bar'
          }
        ]
      });
    }
  }
};
</script>
