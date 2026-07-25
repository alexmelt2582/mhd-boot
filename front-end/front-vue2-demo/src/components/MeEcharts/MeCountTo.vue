<template>
  <div class="font-bold text-xl">{{ d.num.toFixed(0) }}</div>
</template>

<script>
import gsap from "gsap";

export default {
  name: "MeCountTo",
  props: {
    value: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      d: {
        num: 0
      }
    };
  },
  mounted() {
    this.animateToValue();
  },
  watch: {
    value(newValue) {
      this.animateToValue();
    }
  },
  methods: {
    animateToValue() {
      gsap.to(this.d, {
        duration: 0.5,
        num: this.value
      });
    }
  }
};
</script>

<style lang="scss" scoped></style>
