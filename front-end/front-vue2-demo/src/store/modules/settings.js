export default {
  namespaced: true,
  state: {
    isCollapse: false
  },
  mutations: {
    setIsCollapse(state, value) {
      state.isCollapse = value;
    }
  }
};
