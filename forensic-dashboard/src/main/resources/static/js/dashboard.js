/**
 * Sentinel Security Dashboard - Shared Utilities
 */

// ===== MODAL MANAGEMENT =====
class ModalManager {
  static openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('show');
      document.body.style.overflow = 'hidden';
    }
  }

  static closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('show');
      document.body.style.overflow = 'auto';
    }
  }

  static closeAllModals() {
    document.querySelectorAll('.modal').forEach(m => m.classList.remove('show'));
    document.body.style.overflow = 'auto';
  }

  static init() {
    // Close modal when clicking outside
    document.querySelectorAll('.modal').forEach(modal => {
      modal.addEventListener('click', function (e) {
        if (e.target === this) {
          ModalManager.closeModal(this.id);
        }
      });

      // Close button
      const closeBtn = modal.querySelector('.modal-close');
      if (closeBtn) {
        closeBtn.addEventListener('click', () => ModalManager.closeModal(modal.id));
      }
    });

    // ESC key to close
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        ModalManager.closeAllModals();
      }
    });
  }
}

// ===== TABLE SORTING & FILTERING =====
class TableManager {
  static sortTable(tableId, columnIndex, ascending = true) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.sort((a, b) => {
      const aCell = a.cells[columnIndex].textContent.trim();
      const bCell = b.cells[columnIndex].textContent.trim();

      // Try numeric comparison first
      const aNum = parseFloat(aCell);
      const bNum = parseFloat(bCell);

      if (!isNaN(aNum) && !isNaN(bNum)) {
        return ascending ? aNum - bNum : bNum - aNum;
      }

      // String comparison
      if (ascending) {
        return aCell.localeCompare(bCell);
      } else {
        return bCell.localeCompare(aCell);
      }
    });

    // Update sort indicator
    table.querySelectorAll('th').forEach((th, i) => {
      th.classList.remove('sort-asc', 'sort-desc');
      if (i === columnIndex) {
        th.classList.add(ascending ? 'sort-asc' : 'sort-desc');
      }
    });

    tbody.innerHTML = '';
    rows.forEach(row => tbody.appendChild(row));
  }

  static filterTable(tableId, searchValue, columnIndex = -1) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const searchLower = searchValue.toLowerCase();
    const rows = table.querySelectorAll('tbody tr');

    rows.forEach(row => {
      let match = false;

      if (columnIndex === -1) {
        // Search all columns
        match = Array.from(row.cells).some(cell =>
          cell.textContent.toLowerCase().includes(searchLower)
        );
      } else {
        // Search specific column
        match = row.cells[columnIndex]?.textContent.toLowerCase().includes(searchLower);
      }

      row.style.display = match ? '' : 'none';
    });
  }

  static initSorting() {
    document.querySelectorAll('th').forEach((th, i) => {
      th.addEventListener('click', () => {
        const table = th.closest('table');
        const isAsc = th.classList.contains('sort-asc');
        TableManager.sortTable(table.id, i, !isAsc);
      });
      th.style.cursor = 'pointer';
    });
  }
}

// ===== SEARCH & FILTER UTILITY =====
class SearchFilter {
  static init(inputId, tableId, columnIndex = -1) {
    const input = document.getElementById(inputId);
    if (!input) return;

    input.addEventListener('input', (e) => {
      TableManager.filterTable(tableId, e.target.value, columnIndex);
    });
  }
}

// ===== PAGINATION =====
class Paginator {
  constructor(tableId, itemsPerPage = 10) {
    this.tableId = tableId;
    this.itemsPerPage = itemsPerPage;
    this.currentPage = 1;
    this.rows = [];
  }

  init() {
    const table = document.getElementById(this.tableId);
    const tbody = table.querySelector('tbody');
    this.rows = Array.from(tbody.querySelectorAll('tr'));
    this.totalPages = Math.ceil(this.rows.length / this.itemsPerPage);
    this.render();
  }

  render() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;

    this.rows.forEach((row, i) => {
      row.style.display = (i >= start && i < end) ? '' : 'none';
    });

    this.renderPaginationControls();
  }

  renderPaginationControls() {
    const existingPagination = document.getElementById(`${this.tableId}-pagination`);
    if (existingPagination) {
      existingPagination.remove();
    }

    const paginationDiv = document.createElement('div');
    paginationDiv.id = `${this.tableId}-pagination`;
    paginationDiv.className = 'pagination mt-20';

    // Previous button
    if (this.currentPage > 1) {
      const prevBtn = document.createElement('a');
      prevBtn.textContent = '← Previous';
      prevBtn.addEventListener('click', () => {
        this.currentPage--;
        this.render();
      });
      paginationDiv.appendChild(prevBtn);
    }

    // Page numbers
    for (let i = 1; i <= this.totalPages; i++) {
      const pageBtn = document.createElement('span');
      pageBtn.textContent = i;
      if (i === this.currentPage) {
        pageBtn.classList.add('active');
      } else {
        pageBtn.style.cursor = 'pointer';
        pageBtn.addEventListener('click', () => {
          this.currentPage = i;
          this.render();
        });
      }
      paginationDiv.appendChild(pageBtn);
    }

    // Next button
    if (this.currentPage < this.totalPages) {
      const nextBtn = document.createElement('a');
      nextBtn.textContent = 'Next →';
      nextBtn.addEventListener('click', () => {
        this.currentPage++;
        this.render();
      });
      paginationDiv.appendChild(nextBtn);
    }

    document.getElementById(this.tableId).parentElement.appendChild(paginationDiv);
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.render();
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.render();
    }
  }
}

// ===== NOTIFICATION SYSTEM =====
class Notification {
  static show(message, type = 'info', duration = 5000) {
    const container = document.getElementById('notification-container');
    if (!container) {
      const div = document.createElement('div');
      div.id = 'notification-container';
      div.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        z-index: 3000;
        display: flex;
        flex-direction: column;
        gap: 10px;
      `;
      document.body.appendChild(div);
    }

    const notif = document.createElement('div');
    notif.className = `alert-${type}`;
    notif.textContent = message;
    notif.style.cssText = `
      animation: slideIn 0.3s ease;
      min-width: 300px;
    `;

    document.getElementById('notification-container').appendChild(notif);

    if (duration > 0) {
      setTimeout(() => {
        notif.style.animation = 'fadeIn 0.3s ease reverse';
        setTimeout(() => notif.remove(), 300);
      }, duration);
    }

    return notif;
  }

  static success(message, duration = 5000) {
    return this.show(message, 'success', duration);
  }

  static error(message, duration = 5000) {
    return this.show(message, 'error', duration);
  }

  static warning(message, duration = 5000) {
    return this.show(message, 'warning', duration);
  }

  static info(message, duration = 5000) {
    return this.show(message, 'info', duration);
  }
}

// ===== CHART UTILITIES =====
class ChartUtil {
  static getChartOptions(type = 'line') {
    const baseColors = ['#00ff88', '#00ffff', '#ff00ff', '#0088ff', '#ffaa00', '#ff0055'];

    return {
      colors: baseColors,
      chart: {
        background: 'transparent',
        foreColor: '#c9d1d9',
        toolbar: {
          show: true,
          tools: {
            download: true,
            selection: true,
            zoom: true,
            zoomin: true,
            zoomout: true,
            pan: true,
            reset: true
          }
        }
      },
      stroke: {
        width: 2,
        curve: 'smooth'
      },
      grid: {
        borderColor: '#21262d',
        strokeDashArray: 3,
        show: true
      },
      xaxis: {
        labels: {
          style: {
            colors: '#8b949e',
            fontSize: 12
          }
        }
      },
      yaxis: {
        labels: {
          style: {
            colors: '#8b949e',
            fontSize: 12
          }
        }
      },
      legend: {
        labels: {
          colors: '#c9d1d9'
        },
        position: 'bottom',
        horizontalAlign: 'center'
      },
      tooltip: {
        theme: 'dark',
        style: {
          fontSize: 12
        },
        x: {
          show: true
        },
        y: {
          title: {
            formatter: function (val) {
              return val;
            }
          }
        }
      },
      dataLabels: {
        enabled: false
      }
    };
  }

  static createLineChart(elementId, series, categories) {
    const options = {
      ...this.getChartOptions('line'),
      chart: {
        ...this.getChartOptions('line').chart,
        type: 'line'
      },
      xaxis: {
        ...this.getChartOptions('line').xaxis,
        categories: categories
      }
    };

    new ApexCharts(document.querySelector(`#${elementId}`), {
      series: series,
      ...options
    }).render();
  }

  static createBarChart(elementId, series, categories) {
    const options = {
      ...this.getChartOptions('bar'),
      chart: {
        ...this.getChartOptions('bar').chart,
        type: 'bar'
      },
      xaxis: {
        ...this.getChartOptions('bar').xaxis,
        categories: categories
      }
    };

    new ApexCharts(document.querySelector(`#${elementId}`), {
      series: series,
      ...options
    }).render();
  }

  static createDonutChart(elementId, series, labels) {
    const options = {
      ...this.getChartOptions('donut'),
      chart: {
        ...this.getChartOptions('donut').chart,
        type: 'donut'
      },
      labels: labels,
      plotOptions: {
        pie: {
          donut: {
            size: '75%'
          }
        }
      }
    };

    new ApexCharts(document.querySelector(`#${elementId}`), {
      series: series,
      ...options
    }).render();
  }

  static createPieChart(elementId, series, labels) {
    const options = {
      ...this.getChartOptions('pie'),
      chart: {
        ...this.getChartOptions('pie').chart,
        type: 'pie'
      },
      labels: labels
    };

    new ApexCharts(document.querySelector(`#${elementId}`), {
      series: series,
      ...options
    }).render();
  }
}

// ===== FORM UTILITIES =====
class FormUtil {
  static validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }

  static validateRequired(value) {
    return value && value.trim().length > 0;
  }

  static showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    field.style.borderColor = '#ff0055';
    field.style.boxShadow = '0 0 10px rgba(255, 0, 85, 0.3)';

    let errorDiv = field.nextElementSibling;
    if (!errorDiv || !errorDiv.classList.contains('field-error')) {
      errorDiv = document.createElement('div');
      errorDiv.className = 'field-error';
      errorDiv.style.cssText = 'color: #ff0055; font-size: 12px; margin-top: 4px;';
      field.parentElement.insertBefore(errorDiv, field.nextSibling);
    }
    errorDiv.textContent = message;
  }

  static clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    field.style.borderColor = '';
    field.style.boxShadow = '';

    const errorDiv = field.nextElementSibling;
    if (errorDiv && errorDiv.classList.contains('field-error')) {
      errorDiv.remove();
    }
  }
}

// ===== THEME TOGGLE (Optional) =====
class ThemeToggle {
  static isDarkMode() {
    return localStorage.getItem('theme') === 'dark' || !localStorage.getItem('theme');
  }

  static toggle() {
    const isDark = this.isDarkMode();
    if (isDark) {
      this.setLight();
    } else {
      this.setDark();
    }
  }

  static setDark() {
    document.documentElement.setAttribute('data-theme', 'dark');
    localStorage.setItem('theme', 'dark');
  }

  static setLight() {
    document.documentElement.setAttribute('data-theme', 'light');
    localStorage.setItem('theme', 'light');
  }

  static init() {
    const isDark = this.isDarkMode();
    if (isDark) {
      this.setDark();
    } else {
      this.setLight();
    }
  }
}

// ===== UTILITY FUNCTIONS =====
function formatDate(date) {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
}

function formatTimestamp(ns) {
  return formatDate(ns / 1000000); // Convert ns to ms
}

function formatNumber(num) {
  return new Intl.NumberFormat('en-US').format(num);
}

function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    Notification.success('Copied to clipboard!', 3000);
  }).catch(() => {
    Notification.error('Failed to copy', 3000);
  });
}

function downloadCSV(data, filename = 'export.csv') {
  const csv = data.map(row => Object.values(row).join(',')).join('\n');
  const link = document.createElement('a');
  link.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
  link.download = filename;
  link.click();
}

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', () => {
  ModalManager.init();
  TableManager.initSorting();
  ThemeToggle.init();
});
